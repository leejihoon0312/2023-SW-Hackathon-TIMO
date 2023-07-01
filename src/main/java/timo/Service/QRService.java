package timo.Service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import timo.Domain.Phone;
import timo.Repository.PhoneRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class QRService {

    private final PhoneRepository phoneRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Autowired
    private AmazonS3Client amazonS3Client;

    public boolean findUser(String phoneNumber){
        Phone number = phoneRepository.findByPhoneNumber(phoneNumber);
        if (number != null){
            return true;
        }else{
            return false;
        }
    }

    public String uploadImageFileToAwsS3(BufferedImage bufferedImage, String contentType, String fileName)
            throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String imgFileExt = contentType.substring(contentType.indexOf("/") + 1);
        ImageIO.write(bufferedImage, imgFileExt, byteArrayOutputStream);

        // set metadata
        ObjectMetadata objectMetadata = new ObjectMetadata();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        objectMetadata.setContentLength(bytes.length);
        objectMetadata.setContentType(contentType);

        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            return this.createFile(fileName, inputStream, objectMetadata);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 이미지 업로드에 실패했습니다.");
        }
    }

    /**
     * 파일 생성
     *
     * @param fileName
     * @param inputStream
     * @param objectMetadata
     * @return
     */
    private String createFile(String fileName, InputStream inputStream, ObjectMetadata objectMetadata) {

        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        log.info("AmazonS3 putObject file complete!");

        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

}
