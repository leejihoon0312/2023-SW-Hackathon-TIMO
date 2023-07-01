package timo.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import timo.Service.QRService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

@Controller
@RequiredArgsConstructor
public class QRController {

    private final QRService qrService;



    @GetMapping("/qr")
    @ResponseBody
    public String qr(@RequestParam String phone) throws IOException {

        WebClient client3 = WebClient.builder()
                .baseUrl("https://quickchart.io/qr")
                .build();

        byte[] response = null;

        boolean isUser = qrService.findUser(phone);

        if (isUser){ // 확인

            response = client3.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("text", "https://timo-qr.s3.ap-northeast-2.amazonaws.com/%ED%99%95%EC%9D%B8_%EC%95%88%EB%82%B4%EC%B0%BD.png")
                            .build())
                    .accept(MediaType.IMAGE_PNG)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

        }else { // 미확인
            response = client3.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("text", "https://timo-qr.s3.ap-northeast-2.amazonaws.com/%EB%AF%B8%ED%99%95%EC%9D%B8_%EC%95%88%EB%82%B4%EC%B0%BD.png")
                            .build())
                    .accept(MediaType.IMAGE_PNG)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
        }


        ByteArrayInputStream bis = new ByteArrayInputStream(response);
        BufferedImage bImage2 = ImageIO.read(bis);

        String qrURL = qrService.uploadImageFileToAwsS3(bImage2, "png", "qr.png");

        return qrURL;

    }

}
