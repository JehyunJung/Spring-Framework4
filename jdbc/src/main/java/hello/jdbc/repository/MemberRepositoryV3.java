package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

@Slf4j
public class MemberRepositoryV3 {
    private final DataSource dataSource;

    public MemberRepositoryV3(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
         String sql = "INSERT INTO MEMBER VALUES(?,?)";

         Connection conn = null;
         PreparedStatement pstmt = null;

         try{
             conn = getConnection();
             pstmt = conn.prepareStatement(sql);
             pstmt.setString(1, member.getMemberId());
             pstmt.setInt(2, member.getMoney());
             pstmt.executeUpdate();
             return member;
         } catch (SQLException e) {
             log.error("db error", e);
             throw e;
         }
         finally{
             close(conn, pstmt, null);
         }
     }
    public Member findById(String memberId) throws SQLException {
        String sql = "SELECT * FROM MEMBER WHERE MEMBER_ID =?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs=pstmt.executeQuery();

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString(1));
                member.setMoney(rs.getInt(2));
                return member;
            }
            else
                throw new NoSuchElementException("member not found memberId=" + memberId);

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        }
        finally{
            close(conn, pstmt, rs);
        }
    }

    public void update(String memberId,int money) throws SQLException {
        String sql = "UPDATE MEMBER SET MONEY=? WHERE MEMBER_ID =?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try{
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize=pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        }
        finally{
            close(conn, pstmt, null);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "DELETE FROM MEMBER WHERE MEMBER_ID =?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try{
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memberId);

            int resultSize=pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        }
        finally{
            close(conn, pstmt, null);
        }
    }


    private void close(Connection conn, Statement statement, ResultSet resultSet){
        JdbcUtils.closeResultSet(resultSet);
        JdbcUtils.closeStatement(statement);
        DataSourceUtils.releaseConnection(conn, dataSource);
    }
    private Connection getConnection() throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        log.info("connection= {} , {}", connection,connection.getClass());
        return connection;
    }
}
