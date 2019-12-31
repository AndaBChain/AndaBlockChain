package com.aizone.blockchain.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.aizone.blockchain.core.Block;
import com.aizone.blockchain.core.BlockBody;
import com.aizone.blockchain.core.BlockChain;
import com.aizone.blockchain.core.BlockHeader;
import com.aizone.blockchain.core.Transaction;
import com.aizone.blockchain.net.base.Node;
import com.aizone.blockchain.wallet.Account;

public class BlobUtil {

	// 对象转为数组
	public static byte[] toByte(Object object, String objectType) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(baos);
			out.writeObject(objectType);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return baos.toByteArray();
	}

	// 转换为Account对象
	public static Account toAccount(byte[] bytes) {
		ByteArrayInputStream bais;
		ObjectInputStream in = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			in = new ObjectInputStream(bais);
			return (Account) in.readObject();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 转换为Block对象
	public static Block toBlock(byte[] bytes) {
		ByteArrayInputStream bais;
		ObjectInputStream in = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			in = new ObjectInputStream(bais);
			return (Block) in.readObject();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 转换为Transaction对象
	public static Transaction toTransaction(byte[] bytes) {
		ByteArrayInputStream bais;
		ObjectInputStream in = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			in = new ObjectInputStream(bais);
			return (Transaction) in.readObject();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 转换为BlockBody对象
	public static BlockBody toBlockBody(byte[] bytes) {
		ByteArrayInputStream bais;
		ObjectInputStream in = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			in = new ObjectInputStream(bais);
			return (BlockBody) in.readObject();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 转换为BlockChain对象
	public static BlockChain toBlockChain(byte[] bytes) {
		ByteArrayInputStream bais;
		ObjectInputStream in = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			in = new ObjectInputStream(bais);
			return (BlockChain) in.readObject();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 转换为BlockHeader对象
	public static BlockHeader toBlockHeader(byte[] bytes) {
		ByteArrayInputStream bais;
		ObjectInputStream in = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			in = new ObjectInputStream(bais);
			return (BlockHeader) in.readObject();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 转换为Node对象
	public static Node toNode(byte[] bytes) {
		ByteArrayInputStream bais;
		ObjectInputStream in = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			in = new ObjectInputStream(bais);
			return (Node) in.readObject();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 转换为Integer对象
	public static Integer toInteger(byte[] bytes) {
		ByteArrayInputStream bais;
		ObjectInputStream in = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			in = new ObjectInputStream(bais);
			return (Integer) in.readObject();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// 转换为String对象
		public static String toString(byte[] bytes) {
			ByteArrayInputStream bais;
			ObjectInputStream in = null;
			try {
				bais = new ByteArrayInputStream(bytes);
				in = new ObjectInputStream(bais);
				return (String) in.readObject();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
				return null;
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

}
