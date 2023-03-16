// ��ȡ������Ϣ��ʱ����Ҫ��
// �������Windows.hǰ�棬�����б���
#include <WinSock2.h>
#include <Ws2tcpip.h>
// ��ȡ������������Ϣ��Ҫ�����
#pragma comment(lib,"Iphlpapi.lib")
// ת����ַΪ�ַ����ĺ�����Ҫ�����
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

	// �����Ŀ���Ƕ�ȡAdapter����Ҫ���ڴ�ռ��С����û��������
	// �õ��������õ���Ϣ��
	GetAdaptersAddresses(AF_UNSPEC, 0, NULL, addresses, &outBufLen);
	addresses = (IP_ADAPTER_ADDRESSES*)malloc(outBufLen);

	// �洢DNS��ַ��vector��
	std::vector<CHAR*> vect;

	// ��ȡ���������������á�
	DWORD retVal = GetAdaptersAddresses(AF_INET, GAA_FLAG_SKIP_ANYCAST, NULL, addresses, &outBufLen);
	
	if (retVal == NO_ERROR) {
		PIP_ADAPTER_ADDRESSES current = addresses;
		while (current != NULL) {
			// DNS�ĵ�ַ
			IP_ADAPTER_DNS_SERVER_ADDRESS* dnsAddress = current->FirstDnsServerAddress;
			while (dnsAddress != NULL) {
				// ת��DNS��ַΪ�ַ���
				CHAR dnsAddrText[130] = { 0 };
				in_addr nsAddrIn = ((sockaddr_in*)dnsAddress->Address.lpSockaddr)->sin_addr;
				inet_ntop(AF_INET, &nsAddrIn, dnsAddrText, sizeof(dnsAddrText));
				// �����ַ���ַ�����Vector
				vect.push_back(dnsAddrText);
				// ѭ����ȡ��һ����ַ��
				dnsAddress = dnsAddress->Next;
			}
			current = current->Next;
		}

	}

	free(addresses);

	// �����JNI���ݷ��ش���
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