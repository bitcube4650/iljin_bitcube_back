package iljin.framework.ebid.custom.repository;

import iljin.framework.ebid.custom.entity.TCoInterrelated;
import iljin.framework.ijeas.sm.code.CodeDetail;
import iljin.framework.ijeas.sm.code.CodeDetailKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface TCoInterrelatedRepository extends JpaRepository<TCoInterrelated, String> {
}