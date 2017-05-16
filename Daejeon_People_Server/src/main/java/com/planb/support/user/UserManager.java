package com.planb.support.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;

import com.planb.support.crypto.AES256;
import com.planb.support.crypto.SHA256;
import com.planb.support.utilities.DataBase;
import com.planb.support.utilities.Mail;
import com.planb.support.utilities.MailSubjects;
import com.planb.support.utilities.SessionUtil;
import com.sun.javafx.binding.StringFormatter;

import io.vertx.ext.web.RoutingContext;

public class UserManager {
	private static DataBase database = DataBase.getInstance();
	private static AES256 aes = new AES256("d.df!*&ek@s.Cde/q");
	/*
	 * ID : AES256
	 * Registration ID : AES256
	 * Email, Name, Tel : AES256
	 * PW, Session ID : SHA256
	 */
	private static ResultSet rs;

	public static AES256 getAES256Instance() {
		return aes;
	}

	public boolean signin(String id, String password) {
		/*
		 * 로그인
		 * 성공 시 true, 실패 시 false
		 */
		String encryptedId = aes.encrypt(id);
		String encryptedPassword = SHA256.encrypt(password);

		rs = database.executeQuery("SELECT * FROM account WHERE id='", encryptedId, "' AND password='", encryptedPassword, "'");
		try {
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String getEncryptedIdFromSession(RoutingContext ctx) {
		/*
		 * 세션으로부터 암호화된 id get
		 * 유저의 id를 외래키로 갖는 테이블에 접근하기 위해 사용
		 * 객체 생성 없이도 사용할 수 있도록 static
		 */
		String sessionId = SessionUtil.getClientSessionId(ctx, "UserSession");
		String encryptedSessionId = SHA256.encrypt(sessionId);
		String encryptedId = null;
		
		rs = database.executeQuery("SELECT * FROM account WHERE session_id='", encryptedSessionId, "'");
		try {
			if(rs.next()) {
				encryptedId = rs.getString("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return encryptedId;
	}
	
	@Deprecated
	public static String getRegistrationIdFromSession(RoutingContext ctx) {
		/*
		 * 세션으로부터 FireBase registration ID get
		 */
		String encryptedSessionId = SessionUtil.getClientSessionId(ctx, "UserSession");
		String registrationId = null;
		
		rs = database.executeQuery("SELECT * FROM account WHERE session_id='", encryptedSessionId, "'");
		try {
			rs.next();
			registrationId = rs.getString("registration_id");
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		return aes.decrypt(registrationId);
	}
	
	private String getSessionFromId(String id) {
		/*
		 * DB에서 id로부터 암호화된 session id get
		 * 로그인 시 현재 DB에 세션 키가 있는지 체크하기 위해 사용
		 * 추후 하이브리드 서버로 활용 시 필요한 메소드
		 */
		String encryptedId = aes.encrypt(id);
		String encryptedSessionId = null;
		
		rs = database.executeQuery("SELECT * FROM account WHERE id='", encryptedId, "'");
		try {
			rs.next();
			if(rs.getString("session_id") != null) {
				encryptedSessionId = rs.getString("session_id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return encryptedSessionId;
	}

	private String createSessionId() {
		/*
		 * 다른 계정들과 중복되지 않는 session id 생성
		 */
		String uuid;
		
		while(true) {
			uuid = UUID.randomUUID().toString();
			rs = database.executeQuery("SELECT * FROM account WHERE session_id='", SHA256.encrypt(uuid), "'");
			try {
				if(!rs.next()) {
					break;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return uuid;
	}

	public void registerSessionId(RoutingContext ctx, boolean keepLogin, String id) {
		/*
		 * keepLogin 설정에 따라 세션 혹은 쿠키 설정
		 */
		String sessionId = getSessionFromId(id);
		if(sessionId == null) {
			sessionId = createSessionId();
		}
		String encryptedId = aes.encrypt(id);
		
		if(keepLogin) {
			SessionUtil.createCookie(ctx, "UserSession", sessionId);
		} else {
			SessionUtil.createSession(ctx, "UserSession", sessionId);
		}
		
		String encryptedSessionId = SHA256.encrypt(sessionId);
		database.executeUpdate("UPDATE account SET session_id='", encryptedSessionId, "' WHERE id='", encryptedId, "'");
	}
	
	public boolean isLogined(RoutingContext ctx) {
		return ((getEncryptedIdFromSession(ctx) == null) ? false : true);
	}
	
	public void logout(RoutingContext ctx) {
		/*
		 * 로그아웃, 세션 또는 쿠키에 있는 session id 삭제
		 */
		String encryptedId = getEncryptedIdFromSession(ctx);
		SessionUtil.removeSession(ctx, "UserSession");
		database.executeUpdate("UPDATE account SET session_id=null WHERE id='", encryptedId, "'");
	}
	
	public boolean findIdDemand(String email, String name) {
		String encryptedEmail = aes.encrypt(email);
		String encryptedName = aes.encrypt(name);
		
		rs = database.executeQuery("SELECT id FROM account WHERE email='", encryptedEmail, "' AND name='", encryptedName, "'");
		try {
			if(rs.next()) {
				Random random = new Random();
				String code = StringFormatter.format("%06d", random.nextInt(1000000)).getValue();
				// 인증코드 생성
				
				database.executeUpdate("DELETE FROM email_verify_codes WHERE email='", encryptedEmail, "'");
				database.executeUpdate("INSERT INTO email_verify_codes VALUES('", encryptedEmail, "', '", code, "')");
				// 인증코드 insert or refresh
				
				Mail.sendMail(email, MailSubjects.FIND_ID_DEMAND_SUBJECT.getName(), "코드 : " + code);
				// 인증코드 전송
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean findIdVerify(String email, String code) {
		String encryptedEmail = aes.encrypt(email);
		
		rs = database.executeQuery("SELECT * FROM email_verify_codes WHERE email='", encryptedEmail, "' AND code='", code, "'");
		try {
			if (!rs.next()) {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		database.executeUpdate("DELETE FROM email_verify_codes WHERE email='", encryptedEmail, "' AND code='", code, "'");
		rs = database.executeQuery("SELECT * FROM account WHERE email='", encryptedEmail, "'");
		try {
			rs.next();
			String decryptedId = aes.decrypt(rs.getString("id"));
			Mail.sendMail(email, MailSubjects.FIND_ID_RESULT_SUBJECT.getName(), "ID : " + decryptedId);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private String createTempPassword() {
		String tempPassword;
		String encryptedTempPassword;
		
		while(true) {
			tempPassword = UUID.randomUUID().toString().substring(0, 8);
			encryptedTempPassword = SHA256.encrypt(tempPassword);
			rs = database.executeQuery("SELECT * FROM account WHERE password='", encryptedTempPassword);
			try {
				if(!rs.next()) {
					break;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tempPassword;
	}
	
	public boolean findPasswordDemand(String id, String email, String name) {
		String encryptedId = aes.encrypt(id);
		String encryptedEmail = aes.encrypt(email);
		String encryptedName = aes.encrypt(name);
		
		rs = database.executeQuery("SELECT * FROM email_verify_codes WHERE id='", encryptedId, "' AND email='", encryptedEmail, "' AND name='", encryptedName);
		try {
			if(rs.next()) {
				Random random = new Random();
				String code = StringFormatter.format("%06d", random.nextInt(1000000)).getValue();
				// 인증코드 생성
				
				database.executeUpdate("DELETE FROM email_verify_codes WHERE email='", encryptedEmail, "'");
				database.executeUpdate("INSERT INTO email_verify_codes VALUES('", encryptedEmail, "', '", code, "')");
				// 인증코드 insert or refresh
				Mail.sendMail(email, MailSubjects.FIND_PW_DEMAND_SUBJECT.getName(), "코드 : " + code);
				return true;
			} else {
				return false;
			}
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean findPasswordVerify(String email, String code) {
		String encryptedEmail = aes.encrypt(email);
		
		rs = database.executeQuery("SELECT * FROM email_verify_codes WHERE email='", encryptedEmail, "' AND code='", code, "'");
		try {
			if (rs.next()) {
				database.executeUpdate("DELETE FROM email_verify_codes WHERE email='", encryptedEmail, "' AND code='", code, "'");
				String tempPassword = createTempPassword();
				String encryptedTempPassword = SHA256.encrypt(tempPassword);
				database.executeUpdate("UPDATE account SET password='", encryptedTempPassword, "' WHERE email='", encryptedEmail, "'");
				Mail.sendMail(email, MailSubjects.FIND_PW_RESULT_SUBJECT.getName(), "임시 비밀번호 : " + tempPassword);
				return true;
			} else {
				return false;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean changePassword(String id, String currentPassword, String newPassword) {
		String encryptedId = aes.encrypt(id);
		String encryptedCurrentPassword = SHA256.encrypt(currentPassword);
		String encryptedNewPassword = SHA256.encrypt(newPassword);
		
		rs = database.executeQuery("SELECT * FROM account WHERE id='", encryptedId, "' AND password='", encryptedCurrentPassword, "'");
		try {
			if(rs.next()) {
				database.executeUpdate("UPDATE account SET password='", encryptedNewPassword, "' WHERE id='", encryptedId, "'");
				return true;
			} else {
				return false;
			}
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
