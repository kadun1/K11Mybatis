package com.kosmo.k11mybatis;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import mybatis.MemberVO;
import mybatis.MyBoardDTO;
import mybatis.MybatisDAOImpl;
import mybatis.MybatisMemberImpl;
import mybatis.ParameterDTO;
import util.PagingUtil;

@Controller
public class MybatisController {

	/*
	servlet-context.xml에서 생성한 빈을 자동으로 주입받아
	Mybatis를 사용할 준비를 한다.
	 */
	@Autowired
	private SqlSession sqlSession;
	
	@RequestMapping("/mybatis/list.do")
	public String list(Model model, HttpServletRequest req) {
		
		//파라미터 저장을 위한 DTO객체 생성
		ParameterDTO parameterDTO = new ParameterDTO();
		parameterDTO.setSearchField(req.getParameter("searchField"));
		//parameterDTO.setSearchTxt(req.getParameter("searchTxt"));
		
		//검색어를 스페이스로 구분하는 경우
		ArrayList<String> searchLists = null;
		if(req.getParameter("searchTxt")!=null) {
			searchLists = new ArrayList<String>();
			//검색어는 스페이스로 구분되므로 split()을 통해 문자열배열로 반환한다.
			String[] sTxtArray = req.getParameter("searchTxt").split(" ");
			//배열크기만큼 반복하면서 문자열을 컬렉션에 저장한다.
			for(String str : sTxtArray) {
				searchLists.add(str);
			}
		}
		parameterDTO.setSearchTxt(searchLists);
		System.out.println("검색어:"+searchLists);
		
		//게시물의 갯수를 카운트.
		/*
		서비스객체 역할을 하는 인터페이스에 정의된 추상메소드를 호출하면
		최종적으로 Mapper의 동일한 id속성값을 가진 엘리먼트의 쿼리문이
		실행되어 결과를 반환받게 된다.
		 */
		int totalRecordCount = sqlSession.getMapper(MybatisDAOImpl.class).getTotalCount(parameterDTO);
		System.out.println("totalRecordCOunt="+totalRecordCount);
		
		//페이지 처리를 위한 설정값
		int pageSize = 4;
		int blockPage = 2;
		//전체 페이지수 계산
		int totalPage = (int)Math.ceil((double)totalRecordCount/pageSize);
		//현재페이지에 대한 파라미터 처리 및 시작/끝의 rownum구하기
		int nowPage = req.getParameter("nowPage")==null ? 1 : Integer.parseInt(req.getParameter("nowPage"));
		//select할 게시물의 구간을 계산
		int start = (nowPage-1)*pageSize + 1;
		int end = nowPage * pageSize;
		
		//기존의 형태와는 다르게 DTO객체에 저장한 후 Mapper를 호출한다.
		parameterDTO.setStart(start);
		parameterDTO.setEnd(end);
		
		//리스트페이지에 출력할 게시물 가져오기
		ArrayList<MyBoardDTO> lists = sqlSession.getMapper(MybatisDAOImpl.class).listPage(parameterDTO);
		
		//MyBatis 기본쿼리출력
		String sql = sqlSession.getConfiguration()
				.getMappedStatement("listPage")
					.getBoundSql(parameterDTO).getSql();
		System.out.println(sql);
		//페이지번호 처리
		String pagingImg =
				PagingUtil.pagingImg(totalRecordCount, pageSize, blockPage, nowPage,
					req.getContextPath()+"/mybatis/list.do?");
		model.addAttribute("pagingImg", pagingImg);
		
		//게시물의 줄바꿈 처리
		for(MyBoardDTO dto : lists) {
			
			String temp = dto.getContents().replace("\r\n", "<br/>");
			dto.setContents(temp);
		}
		model.addAttribute("lists", lists);
		return "07Mybatis/list";
	}
	
	//글쓰기페이지 : session 영역을 사용하기 위해 HttpSession을 매개변수로 기술함.
	@RequestMapping("/mybatis/write.do")
	public String write(Model model, HttpSession session, HttpServletRequest req) {
		
		//세션영역에 해당 속성이 있는지 확인
		if(session.getAttribute("siteUserInfo")==null) {
			
			//회원인증에 관련된 속성이 없다면 로그인 페이지로 이동한다.
			model.addAttribute("backUrl", "07Mybatis/write");
			//로그인 후 즉시 쓰기페이지로 로케이션하기위해 뷰경로를 파라미터로 넘겨준다.
			return "redirect:login.do";
		}
		
		return "07Mybatis/write";
	}
	//로그인 페이지
	@RequestMapping("/mybatis/login.do")
	public String login(Model model) {
		return "07Mybatis/login";
	}
	//로그인 처리
	@RequestMapping("/mybatis/loginAction.do")
	public ModelAndView loginAction(HttpServletRequest req, HttpSession session) {
		
		MemberVO vo = sqlSession.getMapper(MybatisMemberImpl.class).login(
				req.getParameter("id"),
				req.getParameter("pass")
			);
		
		ModelAndView mv = new ModelAndView();
		if(vo==null) {
			//로그인 실패한 경우...
			mv.addObject("LoginNG", "아이디/패스워드가 틀렸습니다.");
			mv.setViewName("07Mybatis/login");
			return mv;
		}
		else {
			//로그인에 성공한 경우 세션영역에 VO객체를 저장한다.
			session.setAttribute("siteUserInfo", vo);
		}
		
		String backUrl = req.getParameter("backUrl");
		if(backUrl==null || backUrl.equals("")) {
			//로그인 버튼으로 로그인 한 경우에는 로그인페이지로 이동한다.
			mv.setViewName("07Mybatis/login");
		}
		else {
			//특정 페이지로 진입후 로그인페이지로 이동했다면 기존페이지로 이동해야한다.
			mv.setViewName(backUrl);
		}
		return mv;
	}
	//글쓰기 처리
	@RequestMapping(value="/mybatis/writeAction.do", method=RequestMethod.POST)
	public String writeAction(Model model, HttpServletRequest req, HttpSession session) {
		
		//글쓰기 처리전 로그인을 확인한 후 정보가 없다면 진입을 차단한다.
		if(session.getAttribute("siteUserInfo")==null) {
			
			return "redirect:login.do";
		}
		
		//Mybatis사용
		int result = sqlSession.getMapper(MybatisDAOImpl.class).write(
				req.getParameter("name"),
				req.getParameter("contents"),
				((MemberVO)session.getAttribute("siteUserInfo")).getId()
			);
			System.out.println("입력결과"+result);
		
			return "redirect:list.do";
	}
	//수정 페이지
	@RequestMapping("/mybatis/modify.do")
	public String modify(Model model, HttpServletRequest req, HttpSession session) {
		
		//수정 페이지 진입전 로그인 확인
		if(session.getAttribute("siteUserInfo")==null) {
			return "redirect:login.do";
		}
		//Mapper쪽으로 전달할 파라미터를 저장할 용도의 DTO객체 생성
		ParameterDTO parameterDTO = new ParameterDTO();
		parameterDTO.setBoard_idx(req.getParameter("idx"));
		parameterDTO.setUser_id(
				((MemberVO)session.getAttribute("siteUserInfo")).getId());
		
		//Mapper호출시 DTO객체를 파라미터로 전달
		MyBoardDTO dto = sqlSession.getMapper(MybatisDAOImpl.class).view(parameterDTO);
		
		model.addAttribute("dto", dto);
		return "07Mybatis/modify";
	}
	
	//수정처리
	@RequestMapping("/mybatis/modifyAction.do")
	public String modifyAction(HttpSession session, MyBoardDTO myBoardDTO) {
		
		//수정처리 전 로그인확인
		if(session.getAttribute("siteUserInfo")==null) {
			
			//model.addAttribute("backUrl", "07Mybatis/modify");
			return "redirect:login.do";
		}
		
		//수정폼에서 전송한 모든 폼값을 한꺼번에 저장한 커맨드객체를 사용한다.
		sqlSession.getMapper(MybatisDAOImpl.class).modify(myBoardDTO);
		
		return "redirect:list.do";
	}
	
	@RequestMapping("/mybatis/delete.do")
	public String delete(HttpServletRequest req, HttpSession session) {
		
		//로그인확인
		if(session.getAttribute("siteUserInfo")==null) {
			
			return "redirect:login.do";
		}
		
		//Mybatis사용
		sqlSession.getMapper(MybatisDAOImpl.class).delete(req.getParameter("idx"),((MemberVO)session.getAttribute("siteUserInfo")).getId());
		
		return "redirect:list.do";
	}
}
