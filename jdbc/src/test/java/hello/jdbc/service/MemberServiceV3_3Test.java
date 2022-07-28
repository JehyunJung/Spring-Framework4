package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
class MemberServiceV3_3Test {
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepositoryV3 memberRepositoryV3;
    @Autowired
    private MemberServiceV3_3 memberServiceV3;

    @AfterEach
    void after() throws SQLException {
        memberRepositoryV3.delete(MEMBER_A);
        memberRepositoryV3.delete(MEMBER_B);
        memberRepositoryV3.delete(MEMBER_EX);
    }

   @TestConfiguration
   static class TestConfig{
       @Bean
       DataSource dataSource(){
           return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
       }
       @Bean
       PlatformTransactionManager transactionManager(){
           return new DataSourceTransactionManager(dataSource());
       }
       @Bean
       MemberRepositoryV3 memberRepositoryV3(){
           return new MemberRepositoryV3(dataSource());
       }
       @Bean
       MemberServiceV3_3 memberServiceV3_3(){
           return new MemberServiceV3_3(memberRepositoryV3());
       }
   }

   @Test
   @DisplayName("AOP Proxy 적용여부")
   void AOPCheck(){
       log.info("memberService class ={}", memberServiceV3.getClass());
       log.info("memberRepository class={}", memberRepositoryV3.getClass());
       assertThat(AopUtils.isAopProxy(memberServiceV3)).isTrue();
       assertThat(AopUtils.isAopProxy(memberRepositoryV3)).isFalse();
   }


    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);

        memberRepositoryV3.save(memberA);
        memberRepositoryV3.save(memberB);

        //when
        memberServiceV3.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        //then
        Member findMemberA = memberRepositoryV3.findById(MEMBER_A);
        Member findMemberB = memberRepositoryV3.findById(MEMBER_B);

        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 오류")
    void accountTransferEx() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);

        memberRepositoryV3.save(memberA);
        memberRepositoryV3.save(memberEx);

        //when
        assertThatThrownBy(
                () -> memberServiceV3.accountTransfer(
                        memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);


        //then
        Member findMemberA = memberRepositoryV3.findById(MEMBER_A);
        Member findMemberEx = memberRepositoryV3.findById(MEMBER_EX);

        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberEx.getMoney()).isEqualTo(10000);
    }
}