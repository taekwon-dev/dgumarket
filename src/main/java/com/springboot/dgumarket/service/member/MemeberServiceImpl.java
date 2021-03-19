package com.springboot.dgumarket.service.member;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.springboot.dgumarket.exception.CategoryNotFountException;
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
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by TK YOUN (2020-10-20 오전 8:57)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

@Slf4j
@Service
public class MemeberServiceImpl implements MemberProfileService {

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
    public void doSignUp(SignUpDto signUpDto) {

        // 회원정보 DB 저장
        // members, users, member_roles, member_categories
        mapDtoToEntityDoSignup(signUpDto);
    }

    @Transactional
    @Override
    public boolean doWithdraw(int userId) {

        // 예외처리 시 서비스 레이어에서 처리 하도록 변경 예정 (boolean 값으로 처리하지 않도록)

        // 회원탈퇴 요청한 유저의 정보를 갖고 있는 객체를 불러온다.
        Member member = memberRepository.findById(userId);

        if (member == null) return false;
        // [members] 테이블
        // 개인정보보호지침에 따라서 회원의 핸드폰, 이메일 정보는 일정기간 보호 후 삭제한다. (script)
        // - 회원 상태 변경 (isWithdrawn = 1)
        // 회원 상태 변경 시점 (마지막 수정 시간) --> MySQL 스크립트 / 마지막 수정 시간 & 회원 상태 (1) -> 개인정보 삭제 시점
        member.updateMemberStatus(1);

        // [users] 테이블 : SCG 인증/인가 로직에서 활용 / 연관관계 없는 테이블
        User user = userRepository.findByWebMail(member.getWebMail());
        // 탈퇴 유저의 회원 여부 상태 값을 -> 회원 탈퇴(1)로 변경
        if (user != null) user.updateUserStatus(1);

        return true;
    }

    @Override
    public boolean doCheckWebMail(String webMail) {
        // 웹메일 인증 요청 시
        // 회원 정보 테이블(members)에서 회원 상태인 웹메일 대상으로 (회원 상태 : isWithdrawn 0 인 경우)
        // 웹메일 중복 체크를 진행한다.
        Optional<Member> member = memberRepository.findByWebMailAndIsWithdrawn(webMail, 0);

        logger.debug("member : " + member);

        if (member.isPresent()) {
            // 중복된 이메일 (이미 해당 웹메일로 가입한 계정이 있는 경우)
            return true;
        }

        // 중복되지 않은 이메일 (해당 웹메일로 가입한 계정이 없는 경우)
        return false;
    }

    @Override
    public MemberInfoDto fetchMemberInfo(int userId) {
        Member member = memberRepository.findById(userId);

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
    public void updateMemberInfo(int userId, MemberUpdateDto memberUpdateInfoDto) {
        Member member = memberRepository.findById(userId);

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

    public void mapDtoToEntityDoSignup(SignUpDto signUpDto) {

        Set<Role> roleSet = new HashSet<>();

        // 회원가입 시점에 ROLE_MEMBER의 권한을 부여한다.
        // 2 : ROLE_MEMBER (웹메일, 핸드폰 번호 인증한 회원)
        Role role = roleRepository.findById(2);

        // 관리자 권한을 동적으로 추가할 떄 이 부분 로직 추가
        // 관리자로 회원가입하는 경우 정의 --> 해당 로직에서는 Admin 권한 부여
        // Admin 권한 > Member 권한
        roleSet.add(role);

        // ProductCategory
        // 유저가 입력한 상품 카테고리 고유 아이디가 없는 경우 -> 에외 발생 X
        // 해당 값이 Null 처리 -> member_categories에 삽입되는 데이터가 없다 (쿼리 직업 확인)
        Set<ProductCategory> productCategorySet = productCategoryRepository.findByIdIn(signUpDto.getProductCategories());


        Member member = new Member()
                .builder()
                .webMail(signUpDto.getWebMail())
                .phoneNumber(signUpDto.getPhoneNumber())
                .nickName(signUpDto.getNickName())
                .password(encoder.encode(signUpDto.getPassword()))
                .roles(roleSet)                                       // 회원 권한 - ROLE_MEMBER_APPROVED
                .productCategories(productCategorySet)                // 회원 - 관심 카테고리
                .isWithdrawn(0)                                       // 회원 탈퇴 여부(0 : 회원, 1: 회원 탈퇴)
                .isEnabled(0)                                         // 회원 이용 제한 여부 (0 : 이용 가능, 1 : 이용 제한)
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

        memberRepository.save(member);
        userRepository.save(user);
    }
}