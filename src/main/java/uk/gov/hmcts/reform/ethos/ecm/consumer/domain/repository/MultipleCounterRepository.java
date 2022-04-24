package uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.MultipleCounter;

import java.util.List;

@Repository
@Transactional
public interface MultipleCounterRepository extends JpaRepository<MultipleCounter, Integer> {

    @Procedure("fn_persistentQ_getNextMultipleCountVal")
    int persistentQGetNextMultipleCountVal(String multipleRef);

    @Procedure("fn_persistentQ_InsertFirstMultipleCountVal")
    int persistentQInsertFirstMultipleCountVal(String multipleRef);

    List<MultipleCounter> findByMultipleref(String multipleRef);

}
