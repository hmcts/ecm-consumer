package uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.MultipleErrors;

import java.util.List;

@Repository
@Transactional
public interface MultipleErrorsRepository extends JpaRepository<MultipleErrors, Integer> {

    @Procedure("fn_persistentQ_logMultipleError")
    void persistentQLogMultipleError(String multipleRef, String ethosCaseRef, String description);

    List<MultipleErrors> findByMultipleref(String multipleRef);

    void deleteAllByMultipleref(String multipleRef);
}
