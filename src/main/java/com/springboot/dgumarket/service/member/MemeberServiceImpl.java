package com.springboot.dgumarket.service.member;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.dto.member.*;
import com.springboot.dgumarket.exception.CustomJwtException;
import com.springboot.dgumarket.exception.ErrorMessage;
import com.springboot.dgumarket.exception.InappropriateRequestException;
import com.springboot.dgumarket.exception.aws.AWSImageException;
import com.springboot.dgumarket.exception.notFoundException.PreMemberNotFoundException;
import com.springboot.dgumarket.model.member.*;
import com.springboot.dgumarket.payload.response.ApiResultEntity;
import com.springboot.dgumarket.repository.member.*;
import com.springboot.dgumarket.repository.member.redis.RedisJwtTokenRepository;
import com.springboot.dgumarket.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import com.springboot.dgumarket.dto.product.ProductCategoryDto;
import com.springboot.dgumarket.model.Role;
import com.springboot.dgumarket.model.product.ProductCategory;
import com.springboot.dgumarket.repository.product.ProductCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
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

    private static final String PWD_RESET_PAGE_URL = "/shop/account/find_pwd_newPwd?token=";

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PreMemberRepository preMemberRepository;

    @Autowired
    private PhoneVerificationRepository phoneVerificationRepository;

    @Autowired
    private FindPwdVerificationRepository findPwdVerificationRepository;

    @Autowired
    private RedisJwtTokenRepository redisJwtTokenRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public void doSignUp(SignUpDto signUpDto) {

        // 회원정보 DB 저장
        // members, users, member_roles, member_categories
        mapDtoToEntityDoSignup(signUpDto);
    }

    @Transactional
    @Override
    public void doWithdraw(int userId) {

        // [예외처리]
        // 회원탈퇴 및 이용제재를 받은 유저가 요청한 경우, SCG 서버에서 필터링
        // 회원 식별 체크는 이미 Interceptor에서 진행하고 이 곳으로 넘어온 상황
        Member member = memberRepository.findById(userId);

        // [members] 테이블
        // 개인정보보호지침에 따라서 회원의 핸드폰, 이메일 정보는 일정기간 보호 후 삭제한다.
        // - 회원 상태 변경 (isWithdrawn = 1)
        // 회원 상태 변경 시점 (마지막 수정 시간) --> MySQL 스크립트 / 마지막 수정 시간 & 회원 상태 (1) -> 개인정보 삭제 시점
        member.updateMemberStatus(1);

        // [users] 테이블 : SCG 인증/인가 로직에서 활용 / 연관관계 없는 테이블
        User user = userRepository.findByWebMail(member.getWebMail());

        // 탈퇴 유저의 회원 여부 상태 값을 -> 회원 탈퇴(1)로 변경
        user.updateUserStatus(1);

        // 예비 회원을 관리하는 pre-members에 회원 상태 값을 변경해야 하는 이유는 다음과 같다.
        // 회원탈퇴한 유저는 다시 동일한 웹메일을 통해 회원가입을 진행할 수 있다.
        // pre-members에 이전 웹메일 정보가 남아 있는 경우, 새로운 로우를 생성하지 않고 기존 로우를 그대로 활용하는데,
        // 해당 웹메일을 통해 회원절차를 진행하려면 회원상태가 (비회원 또는 탈퇴한 인원)이어야 하기 때문이다.
        // 따라서 2021-03-22에서 premember가 null이어도 회원 탈퇴 로직에는 영향을 끼치지 않는다.
        PreMember preMember = preMemberRepository.findByWebMail(member.getWebMail());

        if (preMember != null) {
            // (0 : 비회원, 1 : 회원, 2: 탈퇴 회원)
            preMember.updatePreMemberStatus(2);
        }

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

        // [예외처리]
        // 회원탈퇴 및 이용제재를 받은 유저가 요청한 경우, SCG 서버에서 필터링
        // 회원 식별 체크는 이미 Interceptor에서 진행하고 이 곳으로 넘어온 상황


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
        memberInfoDto.setWarn(member.checkWarnActive()); // 경고 유무 추가(by ms)
        memberInfoDto.setProductCategories(productCategoryDtoSet);

        // 회원 관심 카테고리의 ID 리스트로 매핑
        return memberInfoDto;
    }

    @Transactional
    @Override
    public void updateMemberInfo(int userId, MemberUpdateDto memberUpdateInfoDto) {
            Member member = memberRepository.findById(userId);

            // [예외처리]
            // 회원탈퇴 및 이용제재를 받은 유저가 요청한 경우, SCG 서버에서 필터링
            // 회원 식별 체크는 이미 Interceptor에서 진행하고 이 곳으로 넘어온 상황

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

                    // [예외처리]
                    // 주어진 카테고리 ID로 DB에 있는 카테고리 식별 불가능한 경우 (추후보완)
                    ProductCategory productCategory = productCategoryRepository.findById(productCategoryDto.getCategory_id());
                    productCategorySet.add(productCategory);
                });

                member.updateCategories(productCategorySet);
            }

            memberRepository.save(member);
    }

    @Transactional
    @Override
    public ApiResultEntity updatePassword(int userId, ChangePwdDto changePwdDto) {

        // init
        ApiResultEntity apiResultEntity = null;

        Member member = memberRepository.findByIdForChange(userId);

        // 1. 기존 비밀번호 번호가 일치하는 지 확인 (틀린 경우 -> 변경 실패)

        if (!passwordEncoder.matches(changePwdDto.getPrevPassword(), member.getPassword())){
            apiResultEntity = ApiResultEntity
                    .builder()
                    .statusCode(1)
                    .message("[비밀번호 변경 실패]기존 비밀번호의 값이 일치하지 않은 경우")
                    .responseData(null)
                    .build();

            return apiResultEntity;
        }

        // 2. 변경하려는 비밀번호, 새 비밀번호 확인 두 필드의 값이 일치하는 지 확인 (서로 다른 경우 -> 변경 실패)
        if (!changePwdDto.getNewPassword().equals(changePwdDto.getCheckNewPassword())) {
            apiResultEntity = ApiResultEntity
                    .builder()
                    .statusCode(2)
                    .message("[비밀번호 변경 실패]새 비밀번호와 새 비밀번호 확인 값이 서로 일치하지 않은 경우")
                    .responseData(null)
                    .build();

            return apiResultEntity;
        }

        // 3. 기존(=현재) 비밀번호와 새 비밀번호가 동일한 경우 (-> 변경 실패)
        // 새로 지정할 비밀번호가 서로 일치하는 것 전제
        if (changePwdDto.getPrevPassword().equals(changePwdDto.getNewPassword())) {
            apiResultEntity = ApiResultEntity
                    .builder()
                    .statusCode(3)
                    .message("[비밀번호 변경 실패]기존 비밀번호와 새 비밀번호가 동일한 경우")
                    .responseData(null)
                    .build();

            return apiResultEntity;
        }

        // 회원 비밀번호 값 변경
        member.updatePassword(encoder.encode(changePwdDto.getNewPassword()));
        memberRepository.save(member);

        apiResultEntity = ApiResultEntity
                .builder()
                .statusCode(200)
                .message("회원 비밀번호 변경 성공")
                .responseData(null)
                .build();

        return apiResultEntity;
    }

    @Transactional
    @Override
    public ApiResultEntity checkVerificationNunberForPhone(int userId, ChangePhoneDto changePhoneDto) {

        // init
        ApiResultEntity apiResultEntity = null;
        String comparisonValue = null;  // DB에 저장된 인증번호 값
        String inputValue = null;       // 유저가 입력한 인증번호 값

        // status {0 : 인증대기, 1: 인증완료}
        PhoneVerification phoneVerification = phoneVerificationRepository.findByPhoneNumberAndStatusIs(changePhoneDto.getPhoneNumber(), 0);

        if (phoneVerification == null) {
            apiResultEntity = ApiResultEntity
                    .builder()
                    .statusCode(1)
                    .message("[인증 실패]인증 대기 중인 핸드폰 번호, 인증 번호 정보를 찾을 수 없습니다.")
                    .responseData(null)
                    .build();

            return apiResultEntity;
        }

        comparisonValue = phoneVerification.getPhoneVerificationNumber();
        inputValue = changePhoneDto.getVerificationNumber();

        if (comparisonValue.equals(inputValue)) {
            Member member = memberRepository.findByIdForChange(userId);

            // 핸드폰 번호 변경을 요청한 유저의 이전 핸드폰 번호와 동일한 경우 예외처리 필터
            if (member.getPhoneNumber().equals(changePhoneDto.getPhoneNumber())) {
                apiResultEntity = ApiResultEntity
                        .builder()
                        .statusCode(3)
                        .message("[인증 실패]기존 핸드폰 번호와 동일한 번호로 수정할 수 없습니다.")
                        .responseData(null)
                        .build();

                return apiResultEntity;
            }

            // 핸드폰 번호 변경 처리
            member.updatePhoneNumber(changePhoneDto.getPhoneNumber());
            memberRepository.save(member);

            phoneVerification.updateStatus(1);
            phoneVerificationRepository.save(phoneVerification);

        } else {
            apiResultEntity = ApiResultEntity
                    .builder()
                    .statusCode(2)
                    .message("[인증 실패]인증번호가 틀렸습니다.")
                    .responseData(null)
                    .build();

            return apiResultEntity;
        }

        apiResultEntity = ApiResultEntity
                .builder()
                .statusCode(200)
                .message("[인증 성공]회원 핸드폰번호 변경 성공")
                .responseData(null)
                .build();

        return apiResultEntity;
    }

    @Override
    public ApiResultEntity getPhoneNumberForPhoneChange(int userId) {

        // init
        ApiResultEntity apiResultEntity = null;
        String phoneNumber = null;

        Member member = memberRepository.findByIdForChange(userId);

        phoneNumber = member.getPhoneNumber();

        apiResultEntity = ApiResultEntity
                .builder()
                .statusCode(200)
                .message("핸드폰 번호 변경 페이지 접근 시, 핸드폰 번호 조회 성공")
                .responseData(phoneNumber)
                .build();

        return apiResultEntity;

    }

    // 비밀번호 재설정 중 핸드폰 번호 인증
    @Transactional
    @Override
    public ApiResultEntity checkVerificationPhoneForFindPwd(FindPwdDto findPwdDto) {

        // init
        ApiResultEntity apiResultEntity = null;
        String comparisonValue = null;  // DB에 저장된 인증번호 값
        String inputValue = null;       // 유저가 입력한 인증번호 값

        FindPwd findPwd = findPwdVerificationRepository.findByWebMailAndPhoneNumber(findPwdDto.getWebMail(), findPwdDto.getPhoneNumber());

        if (findPwd == null) {
            // 인증번호 대조할 값을 참조할 수 없는 경우 -> 인증 실패
            apiResultEntity = ApiResultEntity.builder()
                    .statusCode(1)
                    .message("[인증 실패] 대조할 인증번호를 참조할 수 없는 경우")
                    .responseData(null)
                    .build();
            return apiResultEntity;
        }

        if (!findPwdDto.getVerificationNumber().equals(findPwd.getPhoneVerificationNumber())) {
            // 인증번호 대조 결과, 발급된 인증번호와 일치하지 않는 경우
            apiResultEntity = ApiResultEntity.builder()
                    .statusCode(2)
                    .message("[인증 실패] 발급된 인증번호와 일치하지 않는 경우 ")
                    .responseData(null)
                    .build();
            return apiResultEntity;
        }


        // 비밀번호 재설정 페이지 접근 시 활용되는 토큰 발생 DB 저장
        // 토큰 부여
        String page_token = jwtUtils.genTokenForFindPwd(findPwdDto.getWebMail());
        findPwd.updateToken(page_token);

        apiResultEntity = ApiResultEntity.builder()
                .statusCode(200)
                .message("[인증 성공] 비밀번호 재설정 위한 핸드폰 인증 성공")
                .responseData(PWD_RESET_PAGE_URL+page_token)
                .build();
        return apiResultEntity;
    }

    // 비밀번호 재설정
    @Transactional
    @Override
    public ApiResultEntity resetPasswordForFindPwd(ResetPwdDto resetPwdDto) {

        // init
        ApiResultEntity apiResultEntity = null;
        String token = null;
        String webMail = null;
        String resetPassword = null;
        String resetPasswordForCheck = null;

        // 비밀번호 정규식에 일치한 값이 아닌 경우 (보류)

        // 새로 설정한 비밀번호, 확인 값이 서로 일치하지 않는 경우
        resetPassword = resetPwdDto.getNewPassword();
        resetPasswordForCheck = resetPwdDto.getCheckNewPassword();

        if (!resetPassword.equals(resetPasswordForCheck)) {

            apiResultEntity = ApiResultEntity.builder()
                    .statusCode(2)
                    .message("[비밀번호 재설정 실패] 새로 설정한 비밀번호, 확인 값이 서로 일치하지 않는 경우")
                    .responseData(null)
                    .build();

            return apiResultEntity;
        }

        // DB 바뀐 새로운 비밀번호 값 업데이트 (-> webMail 값 필요 from 토큰)
        token = resetPwdDto.getToken();

        try {
            // [예외처리]
            // token 값이 "" 상태로 들어온 경우 "java.lang.IllegalArgumentException: JWT String argument cannot be null or empty"

            // 토큰이 유효하지 않는 경우 (-> 재설정 할 수 없는 상황, 이미 페이지는 반환 받은 상태)
            if (!jwtUtils.validateToken(token)) {
                throw new CustomJwtException(errorResponse("[비밀번호 재설정 실패] 비밀번호 재설정 유효기간 초과", 306, "/api/user/find-pwd"));
            } else {
                // 유저의 네임을 토큰으로부터 파싱 후 데이터베이스에 변경된 비밀번호를 업데이트
                // [예외처리] : 회원탈퇴, 이용제재 유저는 이 API 요청 시 Gateway 서버에서 필터링

                webMail = jwtUtils.getUsernameFromToken(token);
                Member member = memberRepository.findByWebMail(webMail);
                member.updatePassword(encoder.encode(resetPassword));
            }

        } catch (JwtException e) {
            // ExpiredJwtException 제외한 나머지 JwtException의 경우
            throw new CustomJwtException(errorResponse("[비밀번호 재설정 실패] 비밀번호 재설정 토큰 이슈가 발생한 경우(ExpiredJwtException 제외)", 306, "/api/user/find-pwd"));
        }


        apiResultEntity = ApiResultEntity.builder()
                .statusCode(200)
                .message("[비밀번호 재설정 성공] 비밀번호 재설정 완료")
                .responseData(null)
                .build();

        return apiResultEntity;
    }

    @Override
    public void uploadProfileImgtoS3(MultipartFile multipartFile, String uploadName) {

        // [예외처리]
        // 회원탈퇴 및 이용제재를 받은 유저가 요청한 경우, SCG 서버에서 필터링
        // 회원 식별 체크는 이미 Interceptor에서 진행하고 이 곳으로 넘어온 상황

        // [예외 & 로그]
        // 예외가 발생했을 때 어떻게 빠른 대처를 할 수 있을까? (주제)


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




        } catch (IOException e) {
            // MultipartFile (클라이언트로부터 전달 받은)
            // multipartFile.getInputStream() 예외
            // AWS S3에 Call을 보내기 전 시점에서 예외가 발생하는 경우
            e.printStackTrace();
            throw new AWSImageException(errorResponse("IOException, 회원 프로필 사진 업로드 API", 350, "/api/user/profile/image-upload"));

        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
            throw new AWSImageException(errorResponse("AmazonServiceException, 회원 프로필 사진 업로드 API", 350, "/api/user/profile/image-upload"));


        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
            throw new AWSImageException(errorResponse("SdkClientException, 회원 프로필 사진 업로드 API", 350, "/api/user/profile/image-upload"));


        } catch (InterruptedException e) {
            // void waitForCompletion() throws InterruptedException;
            e.printStackTrace();
            throw new AWSImageException(errorResponse("InterruptedException, 회원 프로필 사진 업로드 API", 350, "/api/user/profile/image-upload"));

        }

    }

    @Override
    public void deleteProfileImgInS3(String deleteName) {

        // [예외처리]
        // 회원탈퇴 및 이용제재를 받은 유저가 요청한 경우, SCG 서버에서 필터링
        // 따라서 유저 인증에 대한 절차는 필요하지 않다.

        // 프로필 사진의 삭제 경우 -> 원본 사진 삭제 -> (Trigger) -> AWS Lambda로 리사이즈된 사진 삭제
        // origin/user-profile/파일명
        String originFileKey = "origin/user-profile/" + deleteName;

        // DeleteObjetctRequest 활용
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, originFileKey);


        try {

            s3Client.deleteObject(deleteObjectRequest); // Could throw SdkClientException, AmazonServiceException.

        } catch (AmazonServiceException e) {
            // 삭제 할 대상이 AWS S3에 없어도 예외가 발생하지 않는다. (참고)
            e.printStackTrace();
            throw new AWSImageException(errorResponse("AmazonServiceException, 회원 프로필 사진 삭제 API", 351, "/api/user/profile/image-delete"));
        }
    }


    @Transactional
    public void mapDtoToEntityDoSignup(SignUpDto signUpDto) {
        // 회원가입 시점에 마지막에 이미 회원인 유저가 재가입 못하도록 처리

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


        Optional<Member> member = memberRepository.findByWebMailAndIsWithdrawn(signUpDto.getWebMail(), 0);


        // [Exception]
        if (member.isPresent()) throw new InappropriateRequestException(errorResponse("이미 회원가입한 유저가 회원가입 API 요청한 경우", 300, "/api/user/signup"));


        Member newMember = new Member()
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

        // 회원가입한 유저가 회원가입 절차(2단계 페이지 접근) 하지 못하도록 차단하기 위해 상태 값을 회원으로 변경 (on pre-members)
        PreMember preMember = preMemberRepository.findByWebMail(signUpDto.getWebMail());

        // [Exception]
        // preMember == null -> RuntimeException -> Rollback -> 예외처리
        if (preMember == null) throw new PreMemberNotFoundException(errorResponse("회원 절차에 있는 예비 회원정보를 찾을 수 없는 경우", 301, "/api/user/signup"));


        // 회원가입 1차 피드백 수정되면 그 때 처리.
        if (preMember != null) {
            // (0 : 비회원, 1 : 회원, 2: 탈퇴 회원)
            preMember.updatePreMemberStatus(1);
        }

        // 회원 테이블 저장
        memberRepository.save(newMember);

        // Gateway 서버에서 활용하는 인증/인가 위한 회원 테이블 저장
        userRepository.save(user);
    }

    // 유저 프로필 관련 API 예외 메시지 오브젝트 생성 및 리턴
    public String errorResponse(String errMsg, int resultCode, String requestPath) {

        // [ErrorMessage]
        // {
        //     int statusCode;
        //     Date timestamp;
        //     String message;
        //     String requestPath;
        //     String pathToMove;
        // }

        // errorCode에 따라서 예외 결과 클라이언트가 특정 페이지로 요청해야 하는 경우가 있다.
        // 그 경우 pathToMove 항목을 채운다.

        // init
        ErrorMessage errorMessage = null;

        // 최종 클라이언트에 반환 될 예외 메시지 (JsonObject as String)
        String errorResponse = null;

        // 예외 처리 결과 클라이언트가 이동시킬 페이지 참조 값을 반환해야 하는 경우 에러 코드 범위
        // (300 - 349)
        // 300 : 이미 회원가입한 유저가 회원가입 API 요청한 경우
        // 301 : 회원 절차에 있는 예비 회원정보를 찾을 수 없는 경우
        // 302 : 회원가입 2단계, 3단계 페이지 요청 시, 토큰 유효하지 않거나 토큰 없이 접근한 경우

        // 예외처리 결과 클라이언트가 __페이지를 요청해야 하는 경우
        // 해당 페이지 정보 포함해서 에러 메시지 반환
        if (resultCode >= 300 && resultCode < 350) {
            errorMessage = ErrorMessage
                    .builder()
                    .statusCode(resultCode)
                    .timestamp(new Date())
                    .message(errMsg)
                    .requestPath(requestPath)
                    .pathToMove("/shop/main/index") // 추후 index 페이지 경로 바뀌면 해당 경로 값으로 수정 할 것.
                    .build();
        } else {
            errorMessage = ErrorMessage
                    .builder()
                    .statusCode(resultCode)
                    .timestamp(new Date())
                    .message(errMsg)
                    .requestPath(requestPath)
                    .build();

        }

        Gson gson = new GsonBuilder().create();

        errorResponse = gson.toJson(errorMessage);

        return errorResponse;
    }
}