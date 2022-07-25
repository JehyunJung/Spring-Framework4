package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
class repositoryTest {

    MemberRepositoryV1 repository;

    @BeforeEach
    void beforeEach(){
        /*DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);*/

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        repository = new MemberRepositoryV1(dataSource);

    }

    @Test
    void crud() throws SQLException, InterruptedException {
        //멤버 저장 테스트
        Member member = new Member("memberV0", 10000);
        Member savedMember = repository.save(member);
        assertThat(savedMember).isEqualTo(member);

        //멤버 조회
        Member findMember = repository.findById(savedMember.getMemberId());
        log.info("findMember={}", findMember);
        assertThat(findMember).isEqualTo(savedMember);

        //정보 수정
        repository.update(savedMember.getMemberId(), 20000);
        Member updatedMember = repository.findById(savedMember.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        //정보 제거
        repository.delete(savedMember.getMemberId());
        Assertions.assertThatThrownBy(() -> repository.findById(savedMember.getMemberId())).isInstanceOf(NoSuchElementException.class);
        Thread.sleep(1000);
    }
}