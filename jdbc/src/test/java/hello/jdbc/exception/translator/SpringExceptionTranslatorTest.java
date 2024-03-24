package hello.jdbc.exception.translator;

import static com.sun.crypto.provider.AESCrypt.log;
import static hello.jdbc.connection.ConnectionConst.*;

import hello.jdbc.connection.ConnectionConst;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

@Slf4j
public class SpringExceptionTranslatorTest {

	DataSource dataSource;

	@BeforeEach
	void inti() {
		dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
	}

	@Test
	void sqlExceptionErrorCode() {
		String sql = "select bad grammar";

		try {
			Connection con = dataSource.getConnection();
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.executeQuery();
		} catch (SQLException e) {
			Assertions.assertThat(e.getErrorCode()).isEqualTo(42122);
			int errorCode = e.getErrorCode();
			log.info("errorCode = {}", errorCode);
			log.info("error", e);
		}
	}

	@Test
	void exceptionTranslator() {
		String sql = "select bad grammar";

		try{
			Connection con = dataSource.getConnection();
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.executeQuery();
		} catch(SQLException e) {
			Assertions.assertThat(e.getErrorCode()).isEqualTo(42122);

			SQLErrorCodeSQLExceptionTranslator exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
			DataAccessException resultEx = exTranslator.translate("select", sql, e);
			log.info("resultEx", resultEx);
			Assertions.assertThat(resultEx.getClass()).isEqualTo(BadSqlGrammarException.class);
		}

	}
}
