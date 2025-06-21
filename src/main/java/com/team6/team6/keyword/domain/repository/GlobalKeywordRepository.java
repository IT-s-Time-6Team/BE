package com.team6.team6.keyword.domain.repository;

import com.team6.team6.keyword.entity.GlobalKeyword;
import com.team6.team6.keyword.entity.KeywordGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GlobalKeywordRepository extends JpaRepository<GlobalKeyword, Long> {
    List<GlobalKeyword> findByKeywordIn(Collection<String> keywords);

    Optional<GlobalKeyword> findByKeyword(String keyword);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE GlobalKeyword g SET g.keywordGroup = :targetGroup WHERE g.keywordGroup IN :oldGroups")
    int bulkUpdateKeywordGroups(@Param("targetGroup") KeywordGroup targetGroup,
                                 @Param("oldGroups") Collection<KeywordGroup> oldGroups);
}
