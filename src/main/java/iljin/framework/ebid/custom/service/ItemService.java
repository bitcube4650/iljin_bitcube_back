package iljin.framework.ebid.custom.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.custom.entity.TCoItem;
import iljin.framework.ebid.custom.entity.TCoItemGrp;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoItemGrpRepository;
import iljin.framework.ebid.custom.repository.TCoItemRepository;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.PagaUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ItemService {

    @Autowired
    private TCoItemRepository tCoItemRepository;
    @Autowired
    private TCoItemGrpRepository tCoItemGrpRepository;
    @Autowired
    private TCoUserRepository tCoUserRepository;

    public List itemGrpList() {
        return tCoItemGrpRepository.findAll();
    }

    public Page itemList(Map<String, Object> params) {
        return tCoItemRepository.findAll(searchWith(params), PagaUtils.pageable(params, "createDate"));
    }

    public Specification<TCoItem> searchWith(Map<String, Object> params) {
        return (Specification<TCoItem>) ((root, query, builder) -> {
            List<Predicate> predicate = getPredicateWithKeyword(params, root, builder);
            return builder.and(predicate.toArray(new Predicate[0]));
        });
    }

    private List<Predicate> getPredicateWithKeyword(Map<String, Object> params, Root<TCoItem> root, CriteriaBuilder builder) {
        List<Predicate> predicate = new ArrayList<>();
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value == null || "".equals(value)) continue;
            switch (key) {
                case "itemGrp":
                    TCoItemGrp itemGrp = tCoItemGrpRepository.findById((String) value).get();
                    predicate.add(builder.equal(root.get(key),itemGrp));
                    break;
                case "useYn":
                    predicate.add(builder.equal(root.get(key),value));
                    break;
                case "itemCode":
                case "itemName":
                    predicate.add(builder.like(root.get(key),"%"+value+"%"));
                    break;
            }
        }
        return predicate;
    }
    public Optional<TCoItem> findById(String id) {
        return tCoItemRepository.findById(id);
    }

    @SneakyThrows
    @Transactional
    public ResultBody save(TCoItem tCoItem) {
        ResultBody resultBody = new ResultBody();
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TCoUser user = tCoUserRepository.findById(principal.getUsername()).get();
        tCoItem.setCreateUser(user);
        log.info(tCoItem.toString());
        tCoItemRepository.save(tCoItem);
        return resultBody;
    }
}
