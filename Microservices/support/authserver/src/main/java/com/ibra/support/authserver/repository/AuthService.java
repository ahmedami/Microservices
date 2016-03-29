package com.ibra.support.authserver.repository;

import com.visualmeta.domain.user.User;
import com.visualmeta.racoon.jdbc.RacoonResultSetExtractor;
import com.visualmeta.racoon.service.Util;
import com.visualmeta.util.Converter;
import com.visualmeta.util.CryptoTools;
import com.ibra.support.authserver.repository.persistence.VisualMetaSharedSchemaJdbcTemplateDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService
{
	private final RacoonResultSetExtractor<User> racoonResultSetExtractor = new RacoonResultSetExtractor<User>(User.class);
	@Autowired
	private VisualMetaSharedSchemaJdbcTemplateDaoImpl jdbcTemplateDao;

	private JdbcTemplate getJdbcTemplate()
	{
		return jdbcTemplateDao.getJdbcTemplate();
	}

	public User authenticate(String email, String password)
	{
		User user = getUserByEmail(email);
		if (user == null) return null;
		if (validatePassword(user, password)) return user;
		return null;
	}

	public User findByEmail(String email, String password)
	{
		User user = getUserByEmail(email);
		if (user == null) return null;
		return user;
	}
	/**
	 * Confirm the password provided is correct to what we have from the database
	 */
	public boolean validatePassword(User user, String password)
	{
		String userSaltHexString = user.getSalt();

		try
		{
			return validatePBKDF2HashedPassword(password, userSaltHexString, user.getPassword());
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public Map<String, String> saltAndHashPasswordWithPBKDF2(String pwd)
	{
		Map<String, String> saltedAndHashedPwd = new HashMap<String, String>();

		String salt;
		try
		{
			salt = CryptoTools.generateSalt();
			saltedAndHashedPwd.put("salt", salt);
			try
			{
				saltedAndHashedPwd.put("password", CryptoTools.generatePBKDF2Hash(pwd, salt));
			}
			catch (NoSuchAlgorithmException | InvalidKeySpecException e)
			{
				e.printStackTrace();
			}
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		return saltedAndHashedPwd;
	}

	/**
	 * Hash the password the user has given us and compare it with the hash we already have for them If the hashes are the same then we have a valid
	 * password
	 */
	public boolean validatePBKDF2HashedPassword(String givenPassword, String salt, String hashedStoredPassword)
			throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		if (hashedStoredPassword.isEmpty())
		{
			return false;
		}

		String[] storedParts = hashedStoredPassword.split(":");

		int iterations = Integer.parseInt(storedParts[0]);
		byte[] hashForStoredPassword = Converter.hexStringTobyteArray(storedParts[1]);

		String hashedGivenPassword = CryptoTools.generatePBKDF2Hash(iterations, givenPassword, salt);
		String[] givenParts = hashedGivenPassword.split(":");
		byte[] hashForGivenPassword = Converter.hexStringTobyteArray(givenParts[1]);

		int diff = hashForStoredPassword.length ^ hashForGivenPassword.length;

		for (int i = 0; i < hashForStoredPassword.length && i < hashForGivenPassword.length; i++)
		{
			diff |= hashForStoredPassword[i] ^ hashForGivenPassword[i];
		}
		return diff == 0;
	}

	public User getUserByEmail(String email)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT * ");
		sb.append(" FROM ").append("user").append(" u ");
		sb.append(" WHERE u.email = '").append(Util.escape(email)).append("'");
		return getJdbcTemplate().query(sb.toString(), racoonResultSetExtractor);
	}

}
