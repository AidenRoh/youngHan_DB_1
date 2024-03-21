package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;
import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DataSource 사용, JdbcUtils 사용
 */
@Slf4j
public class MemberRepositoryV2 {
    private final DataSource dataSource;

    public MemberRepositoryV2(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
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
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt,null);
        }
    }

    public Member findById(String memberId) throws SQLException {
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
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public Member findById(Connection con, String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        PreparedStatement pstmt = null;// finally 를 선언해야 하기 때문에 해당 객체를 사용하기 위해 밖에 선언해야한다.
        ResultSet rs = null;// finally 를 선언해야 하기 때문에 해당 객체를 사용하기 위해 밖에 선언해야한다.

        try {
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
            log.error("db error", e);
            throw e;
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
            // connection은 여기서 닫지 않는다. --> 같은 커넥션 세션에서 비즈니스 로직이 다 수행해야하기 때문에, 커넥션 풀에 반납하는 것을
            // 판단하는 주체는 비즈니스 로직이 해야한다.
        }
    }

    public void update(String memberId, int money) throws SQLException {
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
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void update(Connection con, String memberId, int money) throws SQLException {
        String sql = "update member set money= ? where member_id=?";

        PreparedStatement pstmt = null;


        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            JdbcUtils.closeStatement(pstmt);
            // connection은 여기서 닫지 않는다. --> 같은 커넥션 세션에서 비즈니스 로직이 다 수행해야하기 때문에, 커넥션 풀에 반납하는 것을
            // 판단하는 주체는 비즈니스 로직이 해야한다.
        }
    }

    public void delete(String memberId) throws SQLException {
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
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt,null);
        }
    }

    private void close(Connection con, Statement  stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }
}
