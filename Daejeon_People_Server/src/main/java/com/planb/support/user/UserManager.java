package com.planb.support.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;

import com.planb.support.crypto.AES256;
import com.planb.support.crypto.SHA256;
import com.planb.support.database.DataBase;
import com.planb.support.mail.Mail;
import com.planb.support.mail.MailSubjects;
import com.sun.javafx.binding.StringFormatter;

import io.vertx.ext.web.RoutingContext;

public class UserManager {
	private DataBase database = DataBase.getInstance();
	private AES256 aes = new AES256("d.df!*&ek@s.Cde/q");
	/*
	 * ID : AES256
	 * PW, Email, Name, sessionId : SHA256
	 */
	private ResultSet rs;

	public boolean checkEmailExists(String email) {
		/*
		 *  이메일 존재 여부 체크
		 *  존재 시 true, 실패 시 false
		 */
		String encryptedEmail = SHA256.encrypt(email);

		rs = database.executeQuery("SELECT * FROM account WHERE email='", encryptedEmail, "'");
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

	public void demandEmail(String email) {
		/*
		 * 이메일 전송
		 */
		String encryptedEmail = SHA256.encrypt(email);
		// 이메일 암호화

		Random random = new Random();
		String code = StringFormatter.format("%06d", random.nextInt(1000000)).getValue();
		// 이메일 인증코드 생성
		
		database.executeUpdate("DELETE FROM verify_codes WHERE email='", encryptedEmail, "'");
		database.executeUpdate("INSERT INTO verify_codes VALUES('", encryptedEmail, "', '", code, "')");
		// 인증코드 insert or refresh
		
		Mail.sendMail(email, MailSubjects.VERIFY_SUBJECT.getName(), "코드 : " + code);
		// 인증코드 전송
	}

	public boolean verifyEmail(String email, String code) {
		/*
		 * 인증코드 인증
		 * 성공 시 true, 실패 시 false
		 */
		String encryptedEmail = SHA256.encrypt(email);

		rs = database.executeQuery("SELECT * FROM verify_codes WHERE email='", encryptedEmail, "' AND code='", code, "'");
		try {
			if (rs.next()) {
				database.executeUpdate("DELETE FROM verify_codes WHERE email='", encryptedEmail, "' AND code='", code, "'");
				return true;
			} else {
				return false;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean checkIdExists(String id) {
		/*
		 *  아이디 존재 여부 체크
		 *  존재 시 true, 실패 시 false
		 */
		String encryptedId = aes.encrypt(id);

		rs = database.executeQuery("SELECT * FROM account WHERE id='", encryptedId, "'");
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

	public void signup(String id, String password, String email, String name) {
		/*
		 * 회원가입
		 * id와 이메일 중복 체크는 다른 URI에서 수행
		 */
		String encryptedId = aes.encrypt(id);
		String encryptedPassword = SHA256.encrypt(password);
		String encryptedEmail = SHA256.encrypt(email);
		String encryptedName = SHA256.encrypt(name);

		database.executeUpdate("INSERT INTO account(id, password, email, name) VALUES('", encryptedId, "', '", encryptedPassword, "', '", encryptedEmail, "', '", encryptedName, "')");
		Mail.sendMail(email, MailSubjects.WELCOME_SUBJECT.getName(), "환영환영");
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

	public String getEncryptedIdFromSession(RoutingContext ctx) {
		/*
		 * 세션으로부터 암호화된 id get
		 */
		String encryptedSessionId = SessionUtil.getClientSessionId(ctx, "UserSession");
		String encryptedId = null;
		
		rs = database.executeQuery("SELECT * FROM account WHERE session_id='", encryptedSessionId, "'");
		try {
			rs.next();
			encryptedId = rs.getString("id");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return encryptedId;
	}
	
	private String getEncryptedSessionFromId(String id) {
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

	private String createEncryptedSessionId() {
		/*
		 * 다른 계정들과 중복되지 않는 암호화된 session id 생성
		 */
		String encryptedUUID;
		
		while(true) {
			encryptedUUID = SHA256.encrypt(UUID.randomUUID().toString());
			rs = database.executeQuery("SELECT * FROM account WHERE session_id='", encryptedUUID, "'");
			try {
				if(!rs.next()) {
					break;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return encryptedUUID;
	}

	public void registerSessionId(RoutingContext ctx, boolean keepLogin, String id) {
		/*
		 * keepLogin 설정에 따라 세션 혹은 쿠키 설정
		 */
		String encryptedSessionId = getEncryptedSessionFromId(id);
		if(encryptedSessionId == null) {
			encryptedSessionId = createEncryptedSessionId();
		}
		String encryptedId = aes.encrypt(id);
		
		if(keepLogin) {
			SessionUtil.createCookie(ctx, "UserSession", encryptedSessionId);
		} else {
			SessionUtil.createSession(ctx, "UserSession", encryptedSessionId);
		}
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
		SessionUtil.removeSession(ctx, "UserSession", encryptedId);
		database.executeUpdate("UPDATE account SET session_id=null WHERE id='", encryptedId, "'");
	}
	
	public boolean findId(String email, String name) {
		String encryptedEmail = SHA256.encrypt(email);
		String encryptedName = SHA256.encrypt(name);
		
		rs = database.executeQuery("SELECT id FROM account WHERE email='", encryptedEmail, "' AND name='", encryptedName, "'");
		try {
			if(rs.next()) {
				String decryptedId = aes.decrypt(rs.getString("id"));
				Mail.sendMail(email, MailSubjects.FIND_ID_SUBJECT.getName(), "ID : " + decryptedId);
				return true;
			} else {
				return false;
			}
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
	
	public boolean findPassword(String id, String email, String name) {
		String encryptedId = aes.encrypt(id);
		String encryptedEmail = SHA256.encrypt(email);
		String encryptedName = SHA256.encrypt(name);
		
		rs = database.executeQuery("SELECT * FROM account WHERE id='", encryptedId, "' AND email='", encryptedEmail, "' AND name='", encryptedName, "'");
		try {
			if(rs.next()) {
				String tempPassword = createTempPassword();
				String encryptedTempPassword = SHA256.encrypt(tempPassword);
				database.executeUpdate("UPDATE account SET password='", encryptedTempPassword, "' WHERE id='", encryptedId, "'");
				Mail.sendMail(email, MailSubjects.FIND_PASSWORD_SUBJECT.getName(), "임시 비밀번호 : " + tempPassword);
				return true;
			} else {
				return false;
			}
		} catch(SQLException e) {
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
