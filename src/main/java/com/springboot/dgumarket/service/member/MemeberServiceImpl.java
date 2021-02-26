package com.springboot.dgumarket.service.member;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import org.springframework.beans.factory.annotation.Value;
import com.springboot.dgumarket.dto.member.MemberInfoDto;
import com.springboot.dgumarket.dto.member.MemberUpdateDto;
import com.springboot.dgumarket.dto.member.SignUpDto;
import com.springboot.dgumarket.dto.product.ProductCategoryDto;
import com.springboot.dgumarket.exception.CustomJwtException;
import com.springboot.dgumarket.model.Role;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.member.User;
import com.springboot.dgumarket.model.product.ProductCategory;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.member.RoleRepository;
import com.springboot.dgumarket.repository.member.UserRepository;
import com.springboot.dgumarket.repository.product.ProductCategoryRepository;
import com.springboot.dgumarket.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by TK YOUN (2020-10-20 오전 8:57)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

@Slf4j
@Service
public class MemeberServiceImpl implements MemberService {

    private static final Logger logger = LoggerFactory.getLogger(MemeberServiceImpl.class);

    private static final String S3_SAVED_DIR = "origin/user-profile/";

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Override
    public SignUpDto doSignUp(SignUpDto signUpDto) {

        Member member = mapDtoToEntityDoSignup(signUpDto);
        memberRepository.save(member);
        return null;
    }

    @Override
    public boolean doCheckWebMail(String webMail) {
        Optional<Member> member = memberRepository.findByWebMail(webMail);

        logger.debug("member : " + member);

        if (member.isPresent()) {
            // 중복된 이메일 (이미 해당 웹메일로 가입한 계정이 있는 경우)
            return true;
        }

        // 중복되지 않은 이메일 (해당 웹메일로 가입한 계정이 없는 경우)
        return false;
    }

    @Override
    public MemberInfoDto fetchMemberInfo(int user_id) {
        Member member = memberRepository.findById(user_id);

        org.modelmapper.PropertyMap<ProductCategory, ProductCategoryDto> map_category = new PropertyMap<ProductCategory, ProductCategoryDto>() {
            @Override
            protected void configure() {
                map().setCategory_id(source.getId());
                map().setCategory_name(source.getCategoryName());
            }
        };
        modelMapper = new ModelMapper();
        modelMapper.addMappings(map_category);

        Set<ProductCategoryDto> productCategoryDtoSet= member.getProductCategories()
                .stream()
                .map(productCategory -> modelMapper.map(productCategory, ProductCategoryDto.class))
                .collect(Collectors.toSet());

        MemberInfoDto memberInfoDto = modelMapper.map(member, MemberInfoDto.class);
        memberInfoDto.setProductCategories(productCategoryDtoSet);

        // 회원 관심 카테고리의 ID 리스트로 매핑
        return memberInfoDto;
    }

    @Override
    public void updateMemberInfo(int user_id, MemberUpdateDto memberUpdateInfoDto) {
            Member member = memberRepository.findById(user_id);

        if (memberUpdateInfoDto.getProfileImageDir().isPresent()) {
            member.updateProfileImgDir(memberUpdateInfoDto.getProfileImageDir().get());
        }

        if (memberUpdateInfoDto.getNickName().isPresent()) {
            member.updateNickName(memberUpdateInfoDto.getNickName().get());
        }

        if (memberUpdateInfoDto.getProductCategories().isPresent()) {
            // Dto to Entity
            org.modelmapper.PropertyMap<ProductCategoryDto, ProductCategory> map_category = new PropertyMap<ProductCategoryDto, ProductCategory>() {
                @Override
                protected void configure() {
                    map().setId(source.getCategory_id());
                    map().setCategoryName(source.getCategory_name());
                }
            };
            modelMapper = new ModelMapper();
            modelMapper.addMappings(map_category);

            Set<ProductCategory> productCategorySet = new HashSet<>();
            memberUpdateInfoDto.getProductCategories().get().forEach(productCategoryDto -> {
                ProductCategory productCategory = productCategoryRepository.findById(productCategoryDto.getCategory_id());
                productCategorySet.add(productCategory);
            });

            member.updateCategories(productCategorySet);
        }
        memberRepository.save(member);
    }

    @Override
    public boolean uploadProfileImgtoS3(MultipartFile multipartFile, String uploadName) throws Exception {



        try {
            // https://docs.aws.amazon.com/ko_kr/AmazonS3/latest/userguide/UsingMetadata.html#object-metadata502
            ObjectMetadata metadata  = new ObjectMetadata();
            metadata.setContentType(multipartFile.getContentType());
            metadata.setContentLength(multipartFile.getSize());
            metadata.setHeader("filename", multipartFile.getOriginalFilename());



            // s3 multipart upload
            TransferManager transferManager = TransferManagerBuilder.standard()
                    .withS3Client(s3Client)
                    .build();


            // S3 저장 위치 디렉토리 + 파일명 (고유값)
            String uploadFileName = S3_SAVED_DIR+uploadName;

            // TransferManager processes all transfers asynchronously,
            // so this call returns immediately.
            Upload upload = transferManager.upload(bucketName, uploadFileName, multipartFile.getInputStream(), metadata);
            // Optionally, wait for the upload to finish before continuing.
            upload.waitForCompletion();



        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
            return false;
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteProfileImgInS3(String deleteName) {

        // 프로필 사진의 삭제 경우 -> 원본 사진 삭제 -> (Trigger) -> AWS Lambda로 리사이즈된 사진 삭제
        // origin/user-profile/파일명
        String originFileKey = "origin/user-profile/" + deleteName;

        // DeleteObjetctRequest 활용
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, originFileKey);

        try {
            // 삭제 할 대상이 AWS S3에 없어도 예외가 발생하지 않는다.
            s3Client.deleteObject(deleteObjectRequest); // Could throw SdkClientException, AmazonServiceException.
        } catch (AmazonServiceException e) {
            log.error("프로필 이미지 삭제 요청 중 에러 / 에러 메시지 : " + e.getErrorMessage());
            return false;
        }

        // 예외 없이 이미지 삭제가 된 경우
        return true;
    }

    private Member mapDtoToEntityDoSignup(SignUpDto signUpDto) {

        // Role
        Set<Role> roleSet = new HashSet<>();
        Role role = roleRepository.findById(2); // 2 : ROLE_MEMBER_APPROVED (웹메일, 핸드폰 번호 인증한 회원)
        roleSet.add(role);

        // ProductCategory
        Set<ProductCategory> productCategorySet = new HashSet<>();
        signUpDto.getProductCategories().forEach(category_id -> {
            ProductCategory productCategory = productCategoryRepository.findById(category_id)
                    .orElseThrow(()-> new CustomJwtException("Not Found Product Category ID : " + category_id));

            productCategorySet.add(productCategory);
        });

        Member member = new Member()
                .builder()
                .webMail(signUpDto.getWebMail())
                .phoneNumber(signUpDto.getPhoneNumber())
                .nickName(signUpDto.getNickName())
                .password(encoder.encode(signUpDto.getPassword()))
                .roles(roleSet)                                       // 회원 권한 - ROLE_MEMBER_APPROVED
                .productCategories(productCategorySet)  // 회원 - 관심 카테고리
                .isWithdrawn(0)                                      // 회원 탈퇴 여부(0 : 회원, 1: 회원 탈퇴)
                .isEnabled(0)                                        // 회원 이용 제한 여부 (0 : 이용 가능, 1 : 이용 제한)
                .build();

        User user = User
                .builder()
                .webMail(signUpDto.getWebMail())
                .phoneNumber(signUpDto.getPhoneNumber())
                .password(encoder.encode(signUpDto.getPassword()))
                .nickName(signUpDto.getNickName())
                .roles(role.getName()) // String (두 개 이상인 경우 리스트 to String)
                .isWithdrawn(0)
                .isEnabled(0)
                .build();

        userRepository.save(user);

        return member;
    }
}