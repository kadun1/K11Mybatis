package mybatis;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

@Service
public interface MybatisDAOImpl {
	
	//1차버전에서 사용
	//게시물 갯수 카운트하기
//	public int getTotalCount();
	//게시물을 select해서 List로 반환하기
//	public ArrayList<MyBoardDTO> listPage(int s, int e);
	
	//2차버전에서 사용
	public int getTotalCount(ParameterDTO parameterDTO);
	public ArrayList<MyBoardDTO> listPage(ParameterDTO parameterDTO);
	/*
	Mapper에서 파라미터를 전달한 이름 그대로 사용할수 있도록 @Param
	어노테이션을 사용한다.
	 */
	public int write(@Param("_name") String name,
				@Param("_contents") String contents,
				@Param("_id") String id);

	//기존 게시물 조회
	public MyBoardDTO view(ParameterDTO parameterDTO);

	/*
	Mapper에서 updatem delete는 모두 정수형의 반환값이 있지만
	필요없는 경우 사용하지 않아도 된다.
	 */
	public int modify(MyBoardDTO myBoardDTO);
	public int delete(String idx, String id);
}
