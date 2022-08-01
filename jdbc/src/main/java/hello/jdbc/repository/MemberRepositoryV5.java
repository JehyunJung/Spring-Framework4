package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

public class MemberRepositoryV5 implements MemberRepository{

    private final JdbcTemplate template;

    public MemberRepositoryV5(DataSource datasource) {
        this.template = new JdbcTemplate(datasource);
    }

    @Override
    public Member save(Member member) {
        String sql = "INSERT INTO MEMBER VALUES(?,?)";
        int update = template.update(sql, member.getMemberId(), member.getMoney());
        return member;
    }

    @Override
    public Member findById(String memberId) {
        String sql = "SELECT * FROM MEMBER WHERE MEMBER_ID =?";
        Member member = template.queryForObject(sql, memberRowMapper(), memberId);
        return member;
    }
    private RowMapper<Member> memberRowMapper() {
        return (rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "UPDATE MEMBER SET MONEY=? WHERE MEMBER_ID =?";
        template.update(sql, money, memberId);
    }

    @Override
    public void delete(String memberId) {
        String sql = "DELETE FROM MEMBER WHERE MEMBER_ID =?";
        template.update(sql, memberId);

    }
}
