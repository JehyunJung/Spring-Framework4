package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 memberRepositoryV0 = new MemberRepositoryV0();
    @Test
    void crud() throws SQLException {
        //멤버 저장 테스트
        Member member = new Member("memberV0", 10000);
        Member savedMember = memberRepositoryV0.save(member);
        assertThat(savedMember).isEqualTo(member);

        //멤버 조회
        Member findMember = memberRepositoryV0.findById(savedMember.getMemberId());
        log.info("findMember={}", findMember);
        assertThat(findMember).isEqualTo(savedMember);

        //정보 수정
        memberRepositoryV0.update(savedMember.getMemberId(), 20000);
        Member updatedMember = memberRepositoryV0.findById(savedMember.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        //정보 제거
        memberRepositoryV0.delete(savedMember.getMemberId());
        Assertions.assertThatThrownBy(() -> memberRepositoryV0.findById(savedMember.getMemberId())).isInstanceOf(NoSuchElementException.class);
    }
}