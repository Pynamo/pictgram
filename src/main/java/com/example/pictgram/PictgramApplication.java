package com.example.pictgram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class PictgramApplication extends SpringBootServletInitializer {

	/**
	 * アプリケーションの起動時に実行される
	 * @param args　使用しない
	 */
	public static void main(String[] args) {
		SpringApplication.run(PictgramApplication.class, args);
	}

	/**
	 * warファイルを作成してTomcatなどのServletコンテナにデプロイする場合に使用し、
	 * SpringApplicationBuilderオブジェクトに、アプリケーションクラスの情報を追加します
	 */

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(PictgramApplication.class);
	}


}
