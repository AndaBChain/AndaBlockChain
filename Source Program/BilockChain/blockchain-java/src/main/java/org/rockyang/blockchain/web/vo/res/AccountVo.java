package org.rockyang.blockchain.web.vo.res;

import org.rockyang.blockchain.account.Account;

/**
 * account VO
 * @author Wang HaiTian
 */
public class AccountVo extends Account {

	@Override
	public String toString() {
		return "AccountVo{" +
				"address='" + address + '\'' +
				", priKey='" + priKey + '\'' +
				'}';
	}
}
