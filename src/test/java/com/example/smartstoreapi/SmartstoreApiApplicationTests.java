package com.example.smartstoreapi;

import com.example.smartstoreapi.api.controller.ApiController;
import com.example.smartstoreapi.api.service.ApiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class SmartstoreApiApplicationTests {

	@Test
	void contextLoads() {
	}

	@DisplayName("이메일 추출 테스트")
	@Test
	public void emailExtractionTest(){

		//given
		final ApiService apiService = new ApiService();
		final String text = "이메일주소는fmiwenjf@naver.com입니다!!";

		//when
		final String email = apiService.extractEmails(text);

		//then
		assertThat(email).isEqualTo("fmiwenjf@naver.com");
	}

}
