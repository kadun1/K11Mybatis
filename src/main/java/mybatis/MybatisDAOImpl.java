package mybatis;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

@Service
public interface MybatisDAOImpl {
	
	//게시물 갯수 카운트하기
	public int getTotalCount();
	//게시물을 select해서 List로 반환하기
	public ArrayList<MyBoardDTO> listPage(int s, int e);

}
