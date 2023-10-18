package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트렌잭션 - 파라미터 연동, 풀을 고려한 종료
 */

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {


    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        Connection con = dataSource.getConnection();
        try {
            con.setAutoCommit(false);//트랜젝션 시작
            //비즈니스 로직
            bizLogic(con, fromId, toId, money);
            con.commit(); //성공시 커밋

        } catch (Exception e) {
            con.rollback(); //실패시 롤백
            throw new IllegalStateException(e);

        } finally {
            release(con);
        }


    }

    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }

    private static void release(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                // 일반적인 커넥션인 경우 close() 시 세션이 없어지면서 모든게 기본 설정값으로 초기화되지만
                // 커넥션 풀을 사용하면 해당 세션이 풀로 돌아가는 것이기 때문에 위에서 설정해둔 false설정이 유지가 된다
                // 이를 고려하여 false로 설정해둔 커넥션 세션 설정값을 true(기본값)으로 돌려준다
                con.close(); // 그런 후에 종료
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }

    private static void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }

}
