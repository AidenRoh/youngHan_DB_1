 package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

 /**
 * SQLExceptionTranslator 추가
 */

@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository{
    private final DataSource dataSource;
    private final SQLExceptionTranslator exTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values (?, ?)";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection(); // --> Connection 객체가 주입됨
            pstmt = con.prepareStatement(sql); // prepareStatement 는 어떤 값이 오든 "" 로 바인딩해서 오기 때문에 SQL Injection 공격을 예방할 수 있다.
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            throw exTranslator.translate("save", sql, e);
        } finally {
            close(con, pstmt,null);
        }
    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";
        Connection con = null; // finally 를 선언해야 하기 때문에 해당 객체를 사용하기 위해 밖에 선언해야한다.
        PreparedStatement pstmt = null;// finally 를 선언해야 하기 때문에 해당 객체를 사용하기 위해 밖에 선언해야한다.
        ResultSet rs = null;// finally 를 선언해야 하기 때문에 해당 객체를 사용하기 위해 밖에 선언해야한다.

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId); // 위에 지정해둔 쿼리 문(String sql) 에 첫번째 ? 표에 memberId 를 넣는다는 말
            rs = pstmt.executeQuery(); // Select 기능 수행하고 ResultSet을 반환해준다. ResultSet에는 Select 쿼리문에 대한 결과를 담고있다.
            if (rs.next()) { // next() 를 사용해야 실제 데이터가 있는 것을 호출해준다.
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId= " + memberId);
            }
        } catch (SQLException e) {
            throw exTranslator.translate("findById", sql, e);
        } finally {
            close(con, pstmt, rs);
        }
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money= ? where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw exTranslator.translate("update", sql, e);
        } finally {
            close(con, pstmt, null);
        }
    }

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize); //0이 나와야만 한다.
        } catch (SQLException e) {
            throw exTranslator.translate("delete", sql, e);
        } finally {
            close(con, pstmt,null);
        }
    }

    private void close(Connection con, Statement  stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        DataSourceUtils.releaseConnection(con, dataSource);
//        JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {
        //주의! 트랜잭션 동기
        Connection con = DataSourceUtils.getConnection(dataSource); //Repository에서 쓰레드 로컬에 보관된 커넥션을 꺼내는 것.
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }
}
