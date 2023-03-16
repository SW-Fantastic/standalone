// 读取网络信息的时候需要的
// 必须放在Windows.h前面，否则有报错。
#include <WinSock2.h>
#include <Ws2tcpip.h>
// 读取网络适配器信息需要此类库
#pragma comment(lib,"Iphlpapi.lib")
// 转换网址为字符串的函数需要此类库
#pragma comment(lib, "ws2_32.lib")

#include <Windows.h>
#include <tchar.h>
#include <iostream>
#include <vector>
#include <iphlpapi.h>

#include "jni.h"
#include "org_swdc_platforms_WindowsBinaryUtils.h"


/*
 * Class:     org_swdc_platforms_WindowsBinaryUtils
 * Method:    dllRegisterServer
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_swdc_platforms_WindowsBinaryUtils_dllRegisterServer
(JNIEnv* env, jclass clazz, jstring path) {

	// Convert JavaString to wchar_t*
	const char * cPath = env->GetStringUTFChars(path, 0);
	int len = MultiByteToWideChar(CP_UTF8, 0, cPath,-1,NULL,0);
	wchar_t* wPath = new wchar_t[len];
	MultiByteToWideChar(CP_UTF8, 0, cPath, -1, wPath, len);

	// Load library
	HMODULE dll = LoadLibrary(wPath);
	if (dll == NULL) {
		return false;
	}

	// register the dll component
	typedef HRESULT(*DllRegisterServer)(void);
	DllRegisterServer regFunc = (DllRegisterServer)GetProcAddress(dll, "DllRegisterServer");
	if (regFunc == NULL) {
		return false;
	}
	HRESULT rst = regFunc();
	
	// Release resources
	FreeLibrary(dll);
	delete[] wPath;
	env->ReleaseStringUTFChars(path, cPath);

	return rst == S_OK;
}

/*
 * Class:     org_swdc_platforms_WindowsBinaryUtils
 * Method:    dllUnRegisterServer
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_swdc_platforms_WindowsBinaryUtils_dllUnRegisterServer
(JNIEnv* env, jclass clazz, jstring path) {

	// Convert JavaString to wchar_t*
	const char* cPath = env->GetStringUTFChars(path, 0);
	int len = MultiByteToWideChar(CP_UTF8, 0, cPath, -1, NULL, 0);
	wchar_t* wPath = new wchar_t[len];
	MultiByteToWideChar(CP_UTF8, 0, cPath, -1, wPath, len);

	// Load library
	HMODULE dll = LoadLibrary(wPath);
	if (dll == NULL) {
		return false;
	}

	// unregister the dll component
	typedef HRESULT(*DllUnregisterServer)(void);
	DllUnregisterServer regFunc = (DllUnregisterServer)GetProcAddress(dll, "DllUnregisterServer");
	if (regFunc == NULL) {
		return false;
	}
	HRESULT rst = regFunc();

	// Release resources
	FreeLibrary(dll);
	delete[] wPath;
	env->ReleaseStringUTFChars(path, cPath);

	return rst == S_OK;

}

/*
 * Class:     org_swdc_platforms_WindowsBinaryUtils
 * Method:    getSystemDNSAddress
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_org_swdc_platforms_WindowsBinaryUtils_getSystemDNSAddress
(JNIEnv* env, jclass clazz) {

	PIP_ADAPTER_ADDRESSES addresses = NULL;
	ULONG outBufLen = 0;

	// 这里的目的是读取Adapter所需要的内存空间大小，并没有真正的
	// 得到网络配置的信息。
	GetAdaptersAddresses(AF_UNSPEC, 0, NULL, addresses, &outBufLen);
	addresses = (IP_ADAPTER_ADDRESSES*)malloc(outBufLen);

	// 存储DNS地址的vector。
	std::vector<CHAR*> vect;

	// 读取网络适配器的配置。
	DWORD retVal = GetAdaptersAddresses(AF_INET, GAA_FLAG_SKIP_ANYCAST, NULL, addresses, &outBufLen);
	
	if (retVal == NO_ERROR) {
		PIP_ADAPTER_ADDRESSES current = addresses;
		while (current != NULL) {
			// DNS的地址
			IP_ADAPTER_DNS_SERVER_ADDRESS* dnsAddress = current->FirstDnsServerAddress;
			while (dnsAddress != NULL) {
				// 转换DNS地址为字符型
				CHAR dnsAddrText[130] = { 0 };
				in_addr nsAddrIn = ((sockaddr_in*)dnsAddress->Address.lpSockaddr)->sin_addr;
				inet_ntop(AF_INET, &nsAddrIn, dnsAddrText, sizeof(dnsAddrText));
				// 保存地址的字符串到Vector
				vect.push_back(dnsAddrText);
				// 循环读取下一个地址。
				dnsAddress = dnsAddress->Next;
			}
			current = current->Next;
		}

	}

	free(addresses);

	// 常规的JNI数据返回处理。
	jclass stringClazz = env->FindClass("java/lang/String");

	if (vect.size() > 0) {
		jobjectArray arr = env->NewObjectArray(vect.size(), stringClazz, NULL);

		for (int idx = 0; idx < vect.size(); idx++) {
			const CHAR* ch = vect[idx];
			jstring str = env->NewStringUTF(ch);
			env->SetObjectArrayElement(arr, idx, str);
		}

		return arr;
	}

	return env->NewObjectArray(0,stringClazz,NULL);
}