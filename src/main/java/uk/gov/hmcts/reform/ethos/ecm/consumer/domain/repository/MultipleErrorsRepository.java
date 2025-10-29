package uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.MultipleErrors;

import java.util.List;

@Repository
@Transactional
public interface MultipleErrorsRepository extends JpaRepository<MultipleErrors, Integer> {

    @Query(value = "SELECT fn_persistentQ_logMultipleError(:multipleRef, :ethosCaseRef, :description)",
            nativeQuery = true)
    String persistentQLogMultipleError(@Param("multipleRef") String multipleRef,
                                       @Param("ethosCaseRef") String ethosCaseRef,
                                       @Param("description") String description);

    List<MultipleErrors> findByMultipleref(String multipleRef);

}
