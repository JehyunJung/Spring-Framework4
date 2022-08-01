package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV4_1;
import hello.jdbc.repository.MemberRepositoryV4_2;
import hello.jdbc.repository.MemberRepositoryV5;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
class  MemberServiceV4Test {
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private  MemberServiceV4  memberService;

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

   @TestConfiguration
   @RequiredArgsConstructor
   static class TestConfig{
       private final DataSource dataSource;
       @Bean
      /*
       * MemberRepository with Interface --> DIP
       *
       MemberRepositoryV4_1 memberRepository(){
           return new MemberRepositoryV4_1(dataSource);
       }
       */
       /*
       * MemberRepository with Spring Based Exception (Used SpringExceptionTranslator)
       MemberRepositoryV4_2 memberRepository(){
           return new MemberRepositoryV4_2(dataSource);
       }
       */
       MemberRepositoryV5 memberRepository(){
           return new MemberRepositoryV5(dataSource);
       }
       @Bean
        MemberServiceV4 memberService(){
           return new MemberServiceV4(memberRepository());
       }
   }

   @Test
   @DisplayName("AOP Proxy 적용여부")
   void AOPCheck(){
       log.info("memberService class ={}",  memberService.getClass());
       log.info("memberRepository class={}", memberRepository.getClass());
       assertThat(AopUtils.isAopProxy( memberService)).isTrue();
       assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
   }


    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);

        memberRepository.save(memberA);
        memberRepository.save(memberB);

        //when
         memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        //then
        Member findMemberA = memberRepository.findById(MEMBER_A);
        Member findMemberB = memberRepository.findById(MEMBER_B);

        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 오류")
    void accountTransferEx() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);

        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        //when
        assertThatThrownBy(
                () ->  memberService.accountTransfer(
                        memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);


        //then
        Member findMemberA = memberRepository.findById(MEMBER_A);
        Member findMemberEx = memberRepository.findById(MEMBER_EX);

        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberEx.getMoney()).isEqualTo(10000);
    }
}