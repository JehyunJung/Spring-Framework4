package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {
    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepositoryV2;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection conn = dataSource.getConnection();

        try {
            conn.setAutoCommit(false);
            bizlogic(conn,fromId, toId, money);
            conn.commit();
        } catch (Exception ex) {
            log.error("Transfer Failed");
            conn.rollback();
            throw new IllegalStateException(ex);
        }
        finally{
            release(conn);
        }
    }

    private void bizlogic(Connection conn,String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepositoryV2.findById(conn, fromId);
        Member toMember = memberRepositoryV2.findById(conn, toId);

        memberRepositoryV2.update(conn,fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepositoryV2.update(conn,toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

    private void release(Connection conn) {
        if (conn != null) {
            try{
                conn.setAutoCommit(true);
                conn.close();
            }catch(SQLException ex){
                log.error("error", ex);
            }
        }

    }
}
