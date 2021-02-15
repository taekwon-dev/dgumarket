package com.springboot.dgumarket.service.member;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class MemeberServiceImpl implements MemberService {

    private static final Logger logger = LoggerFactory.getLogger(MemeberServiceImpl.class);

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

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private ImageUtils imageUtils;

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

    // 프로필 이미지 업로드 & 리사이즈 처리 & 저장

    @Override
    public String uploadImageAndResize(MultipartFile multipartFile, int user_id) throws IOException, ImageProcessingException {
        int orientation = 1;
        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        String perfixDir = "src\\main\\resources\\static\\images\\user-profile\\" + user_id;
        String uploadDir = "\\280\\..\\770\\..\\1430";

        Path path = Paths.get(perfixDir+uploadDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path); // Prefix 디렉토리 생성
        }

        String realFileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
        BufferedImage image = ImageIO.read(multipartFile.getInputStream());

        //
        Metadata metadata = ImageMetadataReader.readMetadata(multipartFile.getInputStream());
        Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

        if (directory != null) {
            try {
                orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            } catch (MetadataException e) {
                e.fillInStackTrace();
            }
        }

        //
        switch (orientation) {
            case 1 :
                break;
            case 3 :
                image = Scalr.rotate(image, Scalr.Rotation.CW_180, null);
                break;
            case 6 :
                image = Scalr.rotate(image, Scalr.Rotation.CW_90, null);
                break;
            case 8 :
                image = Scalr.rotate(image, Scalr.Rotation.CW_270, null);
                break;
        }
        //

        BufferedImage bufferedImage_280 = imageUtils.resizeImage(image, 280, 280);
        BufferedImage bufferedImage_770 = imageUtils.resizeImage(image, 770, 770);
        BufferedImage bufferedImage_1430 = imageUtils.resizeImage(image, 1430, 1430);

        ImageIO.write(bufferedImage_280, realFileExtension, new File(perfixDir + "\\280\\" + user_id + "." + realFileExtension));
        ImageIO.write(bufferedImage_770, realFileExtension, new File(perfixDir + "\\770\\" + user_id + "." + realFileExtension));
        ImageIO.write(bufferedImage_1430, realFileExtension, new File(perfixDir + "\\1430\\" + user_id + "." + realFileExtension));

        return user_id+"."+realFileExtension;
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
