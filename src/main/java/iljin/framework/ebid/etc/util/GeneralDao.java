package iljin.framework.ebid.etc.util;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class GeneralDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public Object selectGernalObject(String queryId, @SuppressWarnings("rawtypes") Map paramMap) throws Exception {
		return sqlSession.selectOne(queryId, paramMap);
	}
	
	public Object selectGernalCount(String queryId, @SuppressWarnings("rawtypes") Map paramMap) throws Exception {
		int cnt = (int)sqlSession.selectOne(queryId, paramMap);
		return cnt;
	}

	public int insertGernal(String queryId, @SuppressWarnings("rawtypes") Map paramMap) throws Exception {
		return sqlSession.insert(queryId, paramMap);
	}

	public int updateGernal(String queryId, @SuppressWarnings("rawtypes") Map paramMap) throws Exception {
		return sqlSession.update(queryId, paramMap);
	}

	public int deleteGernal(String queryId, @SuppressWarnings("rawtypes") Map paramMap) throws Exception {
		return sqlSession.delete(queryId, paramMap);
	}

	@SuppressWarnings("unchecked")
	public List<Object> selectGernalList(String queryId, @SuppressWarnings("rawtypes") Map paramMap) throws Exception {
		List<Object> result = null;
		if (paramMap != null) {
			result = sqlSession.selectList(queryId, paramMap);
		} else {
			result = sqlSession.selectList(queryId);
		}
		return result;
	}
    
}
