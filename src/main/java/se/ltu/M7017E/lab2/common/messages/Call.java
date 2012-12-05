package se.ltu.M7017E.lab2.common.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Call {
	private String sender;
	private String receiver;
	private String ipSender;
	private int portSender;

	public String toString() {
		return "CALL," + sender + "," + receiver + "," + ipSender + ","
				+ portSender;
	}

	public static Call fromString(String str) {
		String[] tokens = str.split(",");

		return new Call(tokens[1], tokens[2], tokens[3], new Integer(tokens[4]));
	}
}
