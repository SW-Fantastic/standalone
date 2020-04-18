/*
** Java native interface to the Windows Registry API.
** 
** Authored by Timothy Gerard Endres
** <mailto:time@gjt.org>  <http://www.trustice.com>
** 
** This work has been placed into the public domain.
** You may use this work in any way and for any purpose you wish.
**
** THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND,
** NOT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR
** OF THIS SOFTWARE, ASSUMES _NO_ RESPONSIBILITY FOR ANY
** CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
** REDISTRIBUTION OF THIS SOFTWARE. 
** 
*/

package org.swdc.extern.ice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;


/**
 * The Registry class provides is used to load the native
 * library DLL, as well as a placeholder for the top level
 * keys, error codes, and utility methods.
 *
 * 本类加载DLL链接库，同时提供顶层的注册表内容，异常代码，
 * 以及通用的工具方法。
 *
 * 这是一个提供老的类库的重新编译处理的东西，
 * 当前类库是com.ice.jni.registry,用于操作Windows注册表。
 *
 * Fantastic 2020.4.18
 *
 * @version 3.1.3
 *
 */

public class Registry {

	private static final Logger logger = LoggerFactory.getLogger(Registry.class);

	/**
	 * The following statics are the top level keys.
	 * Without these, there is no way to get "into"
	 * the registry, since the RegOpenSubkey() call
	 * requires an existing key which contains the
	 * subkey.
	 *
	 * 顶层的注册表项，没有他们无法读写注册表。
	 */
	public static RegistryKey		HKEY_CLASSES_ROOT;
	public static RegistryKey		HKEY_CURRENT_USER;
	public static RegistryKey		HKEY_LOCAL_MACHINE;
	public static RegistryKey		HKEY_USERS;
	public static RegistryKey		HKEY_PERFORMANCE_DATA;
	public static RegistryKey		HKEY_CURRENT_CONFIG;
	public static RegistryKey		HKEY_DYN_DATA;

	/**
	 * This is a key for ICE's testing purposes.
	 * 测试用字段
	 */
	private static RegistryKey		HKEY_ICE_TESTKEY = null;
	/**
	 * These are predefined keys ($0-$9) used to make the
	 * testing program easier to use (less typing).
	 * 测试用字段
	 */
	private static String[]			preDefines;

	/**
	 * These are the Registry API error codes, which can
	 * be returned via the RegistryException.
	 *
	 * 这些是API异常代码，通过RegistryException返回。
	 */
	public static final int			ERROR_SUCCESS = 0;
	public static final int			ERROR_FILE_NOT_FOUND = 2;
	public static final int			ERROR_ACCESS_DENIED = 5;
	public static final int			ERROR_INVALID_HANDLE = 6;
	public static final int			ERROR_INVALID_PARAMETER = 87;
	public static final int			ERROR_CALL_NOT_IMPLEMENTED = 120;
	public static final int			ERROR_INSUFFICIENT_BUFFER = 122;
	public static final int			ERROR_LOCK_FAILED = 167;
	public static final int			ERROR_TRANSFER_TOO_LONG = 222;
	public static final int			ERROR_MORE_DATA = 234;
	public static final int			ERROR_NO_MORE_ITEMS = 259;
	public static final int			ERROR_BADDB = 1009;
	public static final int			ERROR_BADKEY = 1010;
	public static final int			ERROR_CANTOPEN = 1011;
	public static final int			ERROR_CANTREAD = 1012;
	public static final int			ERROR_CANTWRITE = 1013;
	public static final int			ERROR_REGISTRY_RECOVERED = 1014;
	public static final int			ERROR_REGISTRY_CORRUPT = 1015;
	public static final int			ERROR_REGISTRY_IO_FAILED = 1016;
	public static final int			ERROR_NOT_REGISTRY_FILE = 1017;
	public static final int			ERROR_KEY_DELETED = 1018;

	/**
	 * These are used by dumpHex().
	 * dumpHex方法会使用这些东西。
	 */
	private static final int		ROW_BYTES = 16;
	private static final int		ROW_QTR1 = 3;
	private static final int		ROW_HALF = 7;
	private static final int		ROW_QTR2 = 11;


	/**
	 * This is the last key used by the test program ($$).
	 * 测试用字段
	 */
	private static String			saveKey = null;
	/**
	 * This is a Hashtable which maps nams to the top level keys.
	 * String - Registry的映射关系
	 */
	private static Hashtable<String, RegistryKey>	topLevelKeys = null;


	/**
	 * If true, debug the fv parameters and computation.
	 */
	public boolean		debugLevel;

	/**
	 * Loads the DLL needed for the native methods, creates the
	 * toplevel keys, fills the hashtable that maps various names
	 * to the toplevel keys.
	 * 载入dll并且填充相应的顶级key对象。
	 */

	static {
		try {
			File lib = new File("ICE_JNIRegistry.dll");
			if (!lib.exists()) {
				try {
					Integer bit = Integer.valueOf(System.getProperty("sun.arch.data.model"));
					if (bit == 32) {
						InputStream in = Registry.class.getModule().getResourceAsStream("bin/ICE_JNIRegistry_32.dll");
						FileOutputStream outputStream = new FileOutputStream(lib);
						in.transferTo(outputStream);
						outputStream.close();
					} else {
						InputStream in = Registry.class.getModule().getResourceAsStream("bin/ICE_JNIRegistry_64.dll");
						FileOutputStream outputStream = new FileOutputStream(lib);
						in.transferTo(outputStream);
						outputStream.close();
					}
				} catch (Exception e) {
					logger.error("fail to extract native lib",e);
				}
			}
			System.loadLibrary( "ICE_JNIRegistry" );
		} catch ( UnsatisfiedLinkError e ) {
			logger.error( "ERROR You have not installed the DLL named '" + "ICE_JNIRegistry.DLL'.\n\t" + e.getMessage() );
		} catch ( SecurityException e ) {
			logger.error( "ERROR You do not have permission to load the DLL named '" + "ICE_JNIRegistry.DLL'.\n\t" + e.getMessage() );
		}

		Registry.HKEY_CLASSES_ROOT =
			new RegistryKey( 0x80000000, "HKEY_CLASSES_ROOT" );

		Registry.HKEY_CURRENT_USER =
			new RegistryKey( 0x80000001, "HKEY_CURRENT_USER" );

		Registry.HKEY_LOCAL_MACHINE =
			new RegistryKey( 0x80000002, "HKEY_LOCAL_MACHINE" );

		Registry.HKEY_USERS =
			new RegistryKey( 0x80000003, "HKEY_USERS" );

		Registry.HKEY_PERFORMANCE_DATA =
			new RegistryKey( 0x80000004, "HKEY_PERFORMANCE_DATA" );

		Registry.HKEY_CURRENT_CONFIG =
			new RegistryKey( 0x80000005, "HKEY_CURRENT_CONFIG" );

		Registry.HKEY_DYN_DATA =
			new RegistryKey( 0x80000006, "HKEY_DYN_DATA" );


		Registry.topLevelKeys = new Hashtable<>( 16 );

		topLevelKeys.put( "HKCR",					Registry.HKEY_CLASSES_ROOT );
		topLevelKeys.put( "HKEY_CLASSES_ROOT",		Registry.HKEY_CLASSES_ROOT );

		topLevelKeys.put( "HKCU",					Registry.HKEY_CURRENT_USER );
		topLevelKeys.put( "HKEY_CURRENT_USER",		Registry.HKEY_CURRENT_USER );

		topLevelKeys.put( "HKLM",					Registry.HKEY_LOCAL_MACHINE );
		topLevelKeys.put( "HKEY_LOCAL_MACHINE",		Registry.HKEY_LOCAL_MACHINE );

		topLevelKeys.put( "HKU",					Registry.HKEY_USERS );
		topLevelKeys.put( "HKUS",					Registry.HKEY_USERS );
		topLevelKeys.put( "HKEY_USERS",				Registry.HKEY_USERS );

		topLevelKeys.put( "HKPD",					Registry.HKEY_PERFORMANCE_DATA );
		topLevelKeys.put( "HKEY_PERFORMANCE_DATA",	Registry.HKEY_PERFORMANCE_DATA );

		topLevelKeys.put( "HKCC",					Registry.HKEY_PERFORMANCE_DATA );
		topLevelKeys.put( "HKEY_CURRENT_CONFIG",	Registry.HKEY_PERFORMANCE_DATA );

		topLevelKeys.put( "HKDD",					Registry.HKEY_PERFORMANCE_DATA );
		topLevelKeys.put( "HKEY_DYN_DATA",			Registry.HKEY_PERFORMANCE_DATA );
	}

	/**
	 * Get a top level key by name using the top level key Hashtable.
     *
     * 通过名称获取顶层RegistryKey
	 *
	 * @param keyName The name of the top level key.
	 * @return The top level RegistryKey, or null if unknown keyName.
	 *
	 */

	public static RegistryKey getTopLevelKey( String keyName ) {
		return Registry.topLevelKeys.get( keyName );
	}

	/**
	 * Open a subkey of a given top level key.
     *
     * 打开注册表指定节点的子节点
	 *
	 * @param topKey The top level key containing the subkey.
	 * @param keyName The subkey's name.
	 * @param access The access flag for the newly opened key.
	 * @return The newly opened RegistryKey.
	 *
	 * @see RegistryKey
	 */

	public static RegistryKey openSubkey(RegistryKey topKey, String keyName, int access) {
		try {
			return topKey.openSubKey( keyName, access );
		} catch (Exception ex ) {
			return null;
		}
	}

	/**
	 * Get the description of a Registry error code.
     *
     * 获取ErrorCode的具体描述
	 *
	 * @param errCode The error code from a RegistryException
     *                来自RegisterException的异常码
	 * @return The description of the error code.
	 */

	public static String getErrorMessage( int errCode ) {
		switch ( errCode )
			{
			case ERROR_SUCCESS: return "success";
			case ERROR_FILE_NOT_FOUND: return "key or value not found";
			case ERROR_ACCESS_DENIED: return "access denied";
			case ERROR_INVALID_HANDLE: return "invalid handle";
			case ERROR_INVALID_PARAMETER: return "invalid parameter";
			case ERROR_CALL_NOT_IMPLEMENTED: return "call not implemented";
			case ERROR_INSUFFICIENT_BUFFER: return "insufficient buffer";
			case ERROR_LOCK_FAILED: return "lock failed";
			case ERROR_TRANSFER_TOO_LONG: return "transfer was too long";
			case ERROR_MORE_DATA: return "more data buffer needed";
			case ERROR_NO_MORE_ITEMS: return "no more items";
			case ERROR_BADDB: return "bad database";
			case ERROR_BADKEY: return "bad key";
			case ERROR_CANTOPEN: return "can not open";
			case ERROR_CANTREAD: return "can not read";
			case ERROR_CANTWRITE: return "can not write";
			case ERROR_REGISTRY_RECOVERED: return "registry recovered";
			case ERROR_REGISTRY_CORRUPT: return "registry corrupt";
			case ERROR_REGISTRY_IO_FAILED: return "registry IO failed";
			case ERROR_NOT_REGISTRY_FILE: return "not a registry file";
			case ERROR_KEY_DELETED: return "key has been deleted";
			}

		return "errCode=" + errCode;
	}

	/**
	 * Export the textual definition for a registry key to a file.
	 * The resulting file can be re-loaded via RegEdit.
     *
     * 导出注册表项到注册表文件（*.reg）/注册表脚本。
	 *
	 * @param pathName The pathname of the file into which to export.
	 * @param key The registry key definition to export.
	 * @param descend If true, descend and export all subkeys.
	 *
     * @exception  NoSuchKeyException  Thrown by openSubKey().
     * @exception  RegistryException  Any other registry API error.
	 */

	public static void exportRegistryKey( String pathName, RegistryKey key, boolean descend )
			throws IOException, NoSuchKeyException, RegistryException {
		PrintWriter out =
			new PrintWriter(
				new FileWriter( pathName ) );

		out.println( "REGEDIT4" );
		out.println( "" );

		key.export( out, descend );

		out.flush();
		out.close();
		}


	/**
	 * Print the usage/help information.
	 */

	public static void usage( String message ) {
		if ( message != null ) {
            logger.info( message );
        }

		logger.info( "keys regKey -- print the key names" );
		logger.info( "values regKey -- print the value names" );
		logger.info( "data regKey subKey -- print the key's data" );
		logger.info( "string regKey subKey -- print REG_SZ key's string" );
		logger.info( "setbin regKey subKey  binaryString -- set REG_BINARY" );
		logger.info( "setdw regKey subKey int -- set REG_DWORD" );
		logger.info( "setstr regKey subKey string -- set REG_SZ" );
		logger.info( "setmulti regKey subKey semiColonString -- set REG_MULTI_SZ" );
		logger.info("delkey regKey subKey -- delete key 'subKey' of regKey" );
		logger.info( "delval regKey subKey -- delete value 'subKey' of regKey" );
		logger.info( "export regKey fileName -- export registry key to fileName" );
		logger.info( "expand regKey valueName -- expand string value" );
		
		logger.info("!! -- repeats last command" );
		logger.info("$$ -- re-uses previous keyname" );
		logger.info( "Predefined Key Prefixes: (e.g. $0-9)" );
		for ( int idx = 0 ; idx < Registry.preDefines.length ; ++idx ){
			logger.info( "   $" + idx + "=" + Registry.preDefines[idx] );
        }
	}


	private static void exportKeyCommand(RegistryKey topKey, String keyName, String pathName ) {
		RegistryKey subKey = Registry.openSubKeyVerbose( topKey, keyName, RegistryKey.ACCESS_READ );

		if ( subKey == null ) {
		    return;
        }

		try {
		    Registry.exportRegistryKey( pathName, subKey, true );
		} catch ( IOException ex ) {
			logger.error("IO Exception: '" + ex.getMessage() + "'" );
		} catch ( NoSuchKeyException ex ) {
			logger.error( "Error, encountered non-existent key during export." );
		} catch ( RegistryException ex ) {
			logger.error( "ERROR registry error=" + ex.getErrorCode() + ", " + ex.getMessage() );
		}
	}

	private static void getDataCommand(RegistryKey topKey, String keyName, String valueName ) {
		RegistryKey subKey = Registry.openSubKeyVerbose( topKey, keyName, RegistryKey.ACCESS_READ );

		if ( subKey == null ) {
		    return;
        }

		RegistryValue	data = null;
		try {
		    data = subKey.getValue( valueName );
		} catch ( NoSuchValueException ex ) {
			logger.error( "Value '" + valueName + "' does not exist." );
			return;
		} catch ( RegistryException ex ) {
			logger.error( "ERROR registry error=" + ex.getErrorCode() + ", " + ex.getMessage() );
			return;
		}

		logger.info( "Value '" + valueName + "' is " + data.toString() );
		if ( data instanceof RegStringValue ) {
			RegStringValue val = (RegStringValue) data;
			logger.info( "REG_SZ '" + val.getData() + "'" );
		} else if ( data instanceof RegMultiStringValue ) {
			RegMultiStringValue val = (RegMultiStringValue) data;
			String[] args = val.getData();
			for ( int idx = 0 ; idx < args.length ; ++idx ) {
				logger.info( "REG_MULTI_SZ[" + idx + "] '" + args[idx] + "'" );
            }
		} else if ( data instanceof RegDWordValue ) {
			RegDWordValue val = (RegDWordValue) data;
			HexNumberFormat xFmt = new HexNumberFormat( "XXXXXXXX" );

			logger.info(
				"REG_DWORD"
				+ ( (RegistryValue.REG_DWORD_BIG_ENDIAN
						== val.getType())
							? "_BIG_ENDIAN" : "" )
				+ " '" + val.getData() + "' [x"
				+ xFmt.format( val.getData() ) + "]" );
		} else {
			RegBinaryValue val = (RegBinaryValue) data;
			Registry.dumpHexData(System.err, "REG_BINARY '" + val.getName() + "', len=" + val.getLength(), val.getData(), val.getLength() );
		}
	}

	private static void getStringCommand(RegistryKey topKey, String keyName, String valueName ) {
		RegistryKey subKey = Registry.openSubKeyVerbose( topKey, keyName, RegistryKey.ACCESS_READ );
		if ( subKey == null ){
		    return;
        }
		try {
			String value = subKey.getStringValue( valueName );
			logger.info( "String Value " + valueName + "='" + value + "'" );
		} catch ( RegistryException ex ) {
			logger.error( "ERROR getting value '" + valueName + "', " + ex.getMessage() );
			return;
		}
	}

	private static void expandStringCommand(RegistryKey topKey, String keyName, String valueName ) {
		RegistryKey subKey = Registry.openSubKeyVerbose( topKey, keyName, RegistryKey.ACCESS_READ );
		if ( subKey == null ) {
            return;
        }
		try {
			String value = subKey.getStringValue( valueName );
			logger.info( "String Value " + valueName + "='" + value + "'" );
			value = RegistryKey.expandEnvStrings( value );
			logger.info( "Expanded Value " + valueName + "='" + value + "'" );
		} catch ( RegistryException ex ) {
			logger.error( "ERROR getting value '" + valueName + "', " + ex.getMessage() );
			return;
		}
	}

	private static void deleteKeyCommand(RegistryKey topKey, String keyName, String deleteKeyName ) {
		RegistryKey subKey = Registry.openSubKeyVerbose( topKey, keyName, RegistryKey.ACCESS_WRITE );

		if ( subKey == null ) {
            return;
        }

		try {
		    subKey.deleteSubKey( deleteKeyName );
		} catch ( NoSuchKeyException ex ) {
			logger.error( "Key '" + keyName + "\\" + deleteKeyName + "' does not exist." );
			return;
		} catch ( RegistryException ex ) {
			logger.error( "ERROR deleting key '" + keyName + "', " + ex.getMessage() );
			return;
		}
	}

	private static void deleteValueCommand(RegistryKey topKey, String keyName, String valueName ) {
		RegistryKey subKey = Registry.openSubKeyVerbose( topKey, keyName, RegistryKey.ACCESS_WRITE );

		if ( subKey == null ){
            return;
        }

		try {
		    subKey.deleteValue( valueName );
		} catch ( NoSuchValueException ex ) {
			logger.error( "Value '" + valueName + "' does not exist." );
			return;
		} catch ( RegistryException ex ) {
			logger.error( "ERROR deleting value '" + valueName + "', " + ex.getMessage() );
			return;
		}
	}

	private static void listKeysCommand( RegistryKey topKey, String keyName ) {
		RegistryKey subKey = Registry.openSubKeyVerbose( topKey, keyName, RegistryKey.ACCESS_READ );
		if ( subKey == null ) {
		    return;
        }
		
		try {
			Enumeration<String> iter = subKey.keyElements();
			for ( int kIdx = 0 ; iter.hasMoreElements() ; ++kIdx ) {
				String keyStr = iter.nextElement();
				logger.info( "Subkey[" + kIdx + "] = '" + keyStr + "'" );
			}
		} catch ( RegistryException ex ) {
			logger.error( "ERROR getting key enumerator, " + ex.getMessage() );
			return;
		}
	}

	private static void listValuesCommand( RegistryKey topKey, String keyName ) {
		RegistryKey subKey = Registry.openSubKeyVerbose( topKey, keyName, RegistryKey.ACCESS_READ );

		if ( subKey == null ) {
            return;
        }
		
		try {
			Enumeration<String> iter = subKey.valueElements();
			for ( int kIdx = 0 ; iter.hasMoreElements() ; ++kIdx ) {
				String name = iter.nextElement();
				logger.info( "Value Name[" + kIdx + "] = '" + name + "'" );
			}
		} catch ( RegistryException ex ) {
			logger.error( "ERROR getting value enumerator, " + ex.getMessage() );
			return;
		}
	}

	private static void setDWordCommand(RegistryKey topKey, String keyName, String valueName, String data ) {
		RegistryKey subKey = Registry.openSubKeyVerbose( topKey, keyName, RegistryKey.ACCESS_WRITE );

		if ( subKey == null ) {
            return;
        }

		int anInt;
		try {
		    anInt = Integer.parseInt( data );
		} catch ( NumberFormatException ex ) {
			logger.error( "ERROR bad int: '" + ex.getMessage() + "'" );
			return;
		}

		RegDWordValue val = new RegDWordValue( subKey, valueName );
		val.setData( anInt );
		
		Registry.setValue( subKey, val );
	}
	
	private static void setMultiStringCommand(RegistryKey topKey, String keyName, String valueName, String data ) {
		RegistryKey subKey = Registry.openSubKeyVerbose( topKey, keyName, RegistryKey.ACCESS_WRITE );

		if ( subKey == null ) {
            return;
        }

		String[] strArray = Registry.splitString( data, ";" );

		RegMultiStringValue val = new RegMultiStringValue( subKey, valueName, strArray );

		Registry.setValue( subKey, val );
	}

	private static void setStringCommand(RegistryKey topKey, String keyName, String valueName, String data ) {
		RegistryKey subKey = Registry.openSubKeyVerbose( topKey, keyName, RegistryKey.ACCESS_WRITE );

		if ( subKey == null ) {
            return;
        }

		RegStringValue val = new RegStringValue( subKey, valueName, data );

		Registry.setValue( subKey, val );
	}

	private static void setBinaryCommand(RegistryKey topKey, String keyName, String valueName, String data ) {
		RegistryKey subKey = Registry.openSubKeyVerbose( topKey, keyName, RegistryKey.ACCESS_WRITE );

		if ( subKey == null ){
            return;
        }

		byte[] binData = data.getBytes();
		RegBinaryValue val = new RegBinaryValue( subKey, valueName, binData );
		Registry.setValue( subKey, val );
	}

	private static void createCommand( RegistryKey topKey, String keyName ) {
	    RegistryKey	subKey;
	    try {
			subKey = topKey.createSubKey( keyName, "", RegistryKey.ACCESS_WRITE );
	    } catch ( RegistryException ex ) {
			subKey = null;
			logger.error( "ERROR creating subKey: " + ex.getMessage() );
	    }

		if ( subKey != null ) {
			try {
				subKey.flushKey();
				subKey.closeKey();
			} catch ( RegistryException ex ) {
				subKey = null;
				logger.error( "ERROR flushing and closing key: " + ex.getMessage() );
			}
		}

		if ( subKey != null ) {
			logger.info( "SUCCEEDED " + ( subKey.wasCreated() ? "Creating" : "Opening via create" ) + " Key '" + keyName + "'" );
		} else {
			logger.error( "FAILED Creating Key '" + keyName + "'" );
		}
	}

	private static RegistryKey openSubKeyVerbose( RegistryKey topKey, String keyName, int access ) {
		RegistryKey		subKey = null;

		try {
		    subKey = topKey.openSubKey( keyName, access );
		} catch ( NoSuchKeyException ex ) {
			subKey = null;
			logger.error( "Key '" + keyName + "' does not exist." );
		} catch ( RegistryException ex ) {
			subKey = null;
			logger.error( "ERROR registry error=" + ex.getErrorCode() + ", " + ex.getMessage() );
		}
		return subKey;
	}

	private static void setValue( RegistryKey subKey, RegistryValue value ) {
		try {
			subKey.setValue( value );
			subKey.flushKey();
		} catch ( RegistryException ex ) {
			logger.error( "ERROR setting MULTI_SZ value '" + value.getName() + "', " + ex.getMessage() );
		}
	}

	public static void dumpHexData( PrintStream out, String title, byte[] buf, int numBytes ) {
		PrintWriter wrtr = new PrintWriter( new OutputStreamWriter( out ) );
		Registry.dumpHexData( wrtr, title, buf, 0, numBytes );
	}

	public static void dumpHexData(PrintWriter out, String title, byte[] buf, int offset, int numBytes ) {
		int			rows, residue, i, j;
		byte[]		save_buf= new byte[ ROW_BYTES+2 ];
		char[]		hex_buf = new char[ 4 ];
		char[]		idx_buf = new char[ 8 ];
		char[]		hex_chars = new char[20];
		
		hex_chars[0] = '0';
		hex_chars[1] = '1';
		hex_chars[2] = '2';
		hex_chars[3] = '3';
		hex_chars[4] = '4';
		hex_chars[5] = '5';
		hex_chars[6] = '6';
		hex_chars[7] = '7';
		hex_chars[8] = '8';
		hex_chars[9] = '9';
		hex_chars[10] = 'A';
		hex_chars[11] = 'B';
		hex_chars[12] = 'C';
		hex_chars[13] = 'D';
		hex_chars[14] = 'E';
		hex_chars[15] = 'F';
		
		out.println( title + " - " + numBytes + " bytes." );

		rows = (numBytes + (ROW_BYTES-1)) / ROW_BYTES;
		residue = (numBytes % ROW_BYTES);

		for ( i = 0 ; i < rows ; i++ ) {
			int hexVal = (i * ROW_BYTES);
			idx_buf[0] = hex_chars[ ((hexVal >> 12) & 15) ];
			idx_buf[1] = hex_chars[ ((hexVal >> 8) & 15) ];
			idx_buf[2] = hex_chars[ ((hexVal >> 4) & 15) ];
			idx_buf[3] = hex_chars[ (hexVal & 15) ];

			String idxStr = new String( idx_buf, 0, 4 );
			out.print( idxStr + ": " );
		
			for ( j = 0 ; j < ROW_BYTES ; j++ ) {
				if ( i == (rows - 1) && j >= residue ) {
					save_buf[j] = ' ';
					out.print( "   " );
					if ( j == ROW_QTR1 || j == ROW_HALF || j == ROW_QTR2 ) {
                        out.print( ' ' );
                    }
				} else {
					save_buf[j] = buf[ offset + (i * ROW_BYTES) + j ];
					hex_buf[0] = hex_chars[ (save_buf[j] >> 4) & 0x0F ];
					hex_buf[1] = hex_chars[ save_buf[j] & 0x0F ];
					out.print( hex_buf[0] );
					out.print( hex_buf[1] );
					out.print( ' ' );

					if ( j == ROW_QTR1 || j == ROW_HALF || j == ROW_QTR2 ) {
                        out.print( ' ' );
                    }

					if ( save_buf[j] < 0x20 || save_buf[j] > 0x7E ) {
                        save_buf[j] = (byte) '.';
                    }
				}
			}

			String saveStr = new String( save_buf, 0, j );
			out.println( " | " + saveStr + " |" );
		}
		out.flush();
	}

	/**
	 * Split a string into a string array containing the substrings
	 * between the delimiters.
	 *
	 * NOTE This method WILL <strong>NOT</strong> return an empty
	 * token at the end of the array that is returned, if the string
	 * ends with the delimiter. If you wish to have a property string
	 * array that ends with the delimiter return an empty string at
	 * the end of the array, use <code>vectorString()</code>.
	 */

    public static String[] splitString( String splitStr, String delim ) {
		int	i, count;
		String[]	result;
		StringTokenizer toker;

		toker = new StringTokenizer( splitStr, delim );
		count = toker.countTokens();
		result = new String[ count ];

		for ( i = 0 ; i < count ; ++i ) {
			try {
			    result[i] = toker.nextToken();
			} catch ( NoSuchElementException ex ) {
				result = null;
				break;
			}
		}
		return result;
	}

	public static String[] parseArgumentString( String argStr ) {
		String[] result = null;
		Vector vector = Registry.parseArgumentVector( argStr );

		if ( vector != null && vector.size() > 0 ) {
			result = new String[ vector.size() ];
			vector.copyInto( result );
		}
		return result;
    }

	public static Vector parseArgumentVector( String argStr ) {
		Vector			result = new Vector();
		StringBuffer	argBuf = new StringBuffer();

		boolean backSlash = false;
		boolean matchSglQuote = false;
		boolean matchDblQuote = false;

		for ( int cIdx = 0 ; cIdx < argStr.length() ; ++cIdx ) {
			char ch = argStr.charAt( cIdx );
			switch ( ch )
				{
				//
				// W H I T E S P A C E
				//
				case ' ':
				case '\t':
				case '\n':
				case '\r':
					if ( backSlash ) {
						argBuf.append( ch );
						backSlash = false; 
					} else if ( matchSglQuote || matchDblQuote ) {
						argBuf.append( ch );
					} else if ( argBuf.length() > 0 ) {
						result.addElement( argBuf.toString() );
						argBuf.setLength( 0 );
					}
					break;
				case '\\':
					if ( backSlash ) {
						argBuf.append( "\\" );
					}
					backSlash = ! backSlash;
					break;
				case '\'':
					if ( backSlash ) {
						argBuf.append( "'" );
						backSlash = false; 
					} else if ( matchSglQuote ) {
						result.addElement( argBuf.toString() );
						argBuf.setLength( 0 );
						matchSglQuote = false;
					} else if ( ! matchDblQuote ) {
						matchSglQuote = true;
					}
					break;

				case '"':
					if ( backSlash ) {
						argBuf.append( "\"" );
						backSlash = false; 
					} else if ( matchDblQuote ) {
						result.addElement( argBuf.toString() );
						argBuf.setLength( 0 );
						matchDblQuote = false;
					} else if ( ! matchSglQuote ) {
						matchDblQuote = true;
					}
					break;
				default:
					if ( backSlash ) {
						switch ( ch ) {
							case 'b': argBuf.append( '\b' ); break;
							case 'f': argBuf.append( '\f' ); break;
							case 'n': argBuf.append( '\n' ); break;
							case 'r': argBuf.append( '\r' ); break;
							case 't': argBuf.append( '\t' ); break;

							default:
								char ch2 = argStr.charAt( cIdx+1 );
								char ch3 = argStr.charAt( cIdx+2 );
								if ( (ch >= '0' && ch <= '7')
										&& (ch2 >= '0' && ch2 <= '7')
										&& (ch3 >= '0' && ch3 <= '7') ) {
									int octal =
										( ( (ch - '0') * 64 )
											+ ( (ch2 - '0') * 8 )
												+ (ch3 - '0') );
									argBuf.append( (char) octal );
									cIdx += 2;
								} else if ( ch == '0' ) {
									argBuf.append( '\0' );
								} else {
									argBuf.append( ch );
								}
								break;
						}
					} else {
					    argBuf.append( ch );
					}
					backSlash = false;
					break;
				}
		}
		if ( argBuf.length() > 0 ) {
			result.addElement( argBuf.toString() );
		}
		return result;
    }
	
}




