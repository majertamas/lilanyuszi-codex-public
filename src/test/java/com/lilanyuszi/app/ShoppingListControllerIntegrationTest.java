package com.lilanyuszi.app;

import com.lilanyuszi.app.api.MessageSeverity;
import com.lilanyuszi.app.shared_access.SharedAccess;
import com.lilanyuszi.app.shared_access.SharedAccessRepository;
import com.lilanyuszi.app.shared_access.SharedAccessService;
import com.lilanyuszi.app.shared_access.SharedAccessType;
import com.lilanyuszi.app.shared_access_alias.SharedAccessAlias;
import com.lilanyuszi.app.shared_access_alias.SharedAccessAliasRepository;
import com.lilanyuszi.app.shared_access_member.SharedAccessMember;
import com.lilanyuszi.app.shared_access_member.SharedAccessMemberRepository;
import com.lilanyuszi.app.shopping_item.ShoppingItem;
import com.lilanyuszi.app.shopping_item.ShoppingItemRepository;
import com.lilanyuszi.app.shopping_list.ShoppingList;
import com.lilanyuszi.app.shopping_list.ShoppingListRepository;
import com.lilanyuszi.app.unit.Unit;
import com.lilanyuszi.app.unit.UnitRepository;
import com.lilanyuszi.app.user.User;
import com.lilanyuszi.app.user.UserRepository;
import com.lilanyuszi.app.user.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;

import static com.lilanyuszi.app.util.Constant.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "app.test=shopping-list-controller-integration-test",
        "app.frontend.url.me=https://frontend.example/me",
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"
})
@AutoConfigureMockMvc
class ShoppingListControllerIntegrationTest {

    private static final String OWNER_EMAIL = "owner@example.com";
    private static final String MEMBER_EMAIL = "member@example.com";
    private static final String OUTSIDER_EMAIL = "outsider@example.com";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String WEEKLY_GROCERIES = "Weekly groceries";
    private static final String OWNER_GROCERIES = "Owner groceries";
    private static final String OWNER_PARTY = "Owner party";
    private static final String OUTSIDER_GROCERIES = "Outsider groceries";
    private static final String TESZT = "teszt";
    private static final String TESZT_TITLE_CASE = "Teszt";
    private static final String PADDED_TESZT = " teszt ";
    private static final String HOME_ALIAS = "Home";
    private static final String FLAT_ALIAS = "Flat";
    private static final String MILK = "Milk";
    private static final String PIECE_UNIT = "db";
    private static final String VALID_CREATE_REQUEST = "{\"name\":\"" + WEEKLY_GROCERIES + "\"}";
    private static final String INVALID_CREATE_REQUEST = "{\"name\":\"   \"}";
    private static final String DATA_JSON_PATH = "$.data";
    private static final String MESSAGES_JSON_PATH = "$.messages";
    private static final String ERROR_TEXT_JSON_PATH = "$.messages[0].text";
    private static final String ERROR_SEVERITY_JSON_PATH = "$.messages[0].severity";
    private static final String DATA_ID_JSON_PATH = "$.data.id";
    private static final String DATA_SHARED_ACCESS_ID_JSON_PATH = "$.data.sharedAccessId";
    private static final String DATA_NAME_JSON_PATH = "$.data.name";
    private static final String MEMBERS_ME = "/members/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SharedAccessRepository sharedAccessRepository;

    @Autowired
    private SharedAccessService sharedAccessService;

    @Autowired
    private SharedAccessMemberRepository sharedAccessMemberRepository;

    @Autowired
    private SharedAccessAliasRepository sharedAccessAliasRepository;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private ShoppingItemRepository shoppingItemRepository;

    @Autowired
    private UnitRepository unitRepository;

    @BeforeEach
    void setUp() {
        sharedAccessAliasRepository.deleteAll();
        sharedAccessMemberRepository.deleteAll();
        shoppingItemRepository.deleteAll();
        shoppingListRepository.deleteAll();
        sharedAccessRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createsShoppingListWithValidRequest() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));

        mockMvc.perform(post(API_SHOPPING_LIST_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CREATE_REQUEST)
                        .with(authentication(new TestingAuthenticationToken(owner.getEmail(), null, ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty())
                .andExpect(jsonPath(DATA_ID_JSON_PATH).exists())
                .andExpect(jsonPath(DATA_SHARED_ACCESS_ID_JSON_PATH).exists())
                .andExpect(jsonPath(DATA_NAME_JSON_PATH).value(WEEKLY_GROCERIES));

        ShoppingList shoppingList = shoppingListRepository.findAll().get(0);
        SharedAccess sharedAccess = sharedAccessRepository.findAll().get(0);
        SharedAccessMember member = sharedAccessMemberRepository.findAll().get(0);

        assertEquals(1, shoppingListRepository.count());
        assertEquals(1, sharedAccessRepository.count());
        assertEquals(1, sharedAccessMemberRepository.count());
        assertEquals(sharedAccess.getId(), shoppingList.getSharedAccess().getId());
        assertEquals(sharedAccess.getId(), shoppingList.getId());
        assertEquals(WEEKLY_GROCERIES, sharedAccess.getName());
        assertEquals(canonicalizeName(WEEKLY_GROCERIES), sharedAccess.getCanonicalName());
        assertEquals(SharedAccessType.SHOPPING, sharedAccess.getType());
        assertEquals(owner.getId(), sharedAccess.getOwnerUser().getId());
        assertEquals(sharedAccess.getId(), member.getSharedAccess().getId());
        assertEquals(owner.getId(), member.getUser().getId());
    }

    @Test
    void doesNotCreateShoppingListWithInvalidRequestAndGetsRestResponseWithErrorMessage() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));

        mockMvc.perform(post(API_SHOPPING_LIST_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_CREATE_REQUEST)
                        .with(authentication(new TestingAuthenticationToken(owner.getEmail(), null, ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DATA_JSON_PATH).doesNotExist())
                .andExpect(jsonPath(ERROR_TEXT_JSON_PATH).value(SHOPPING_LIST_NAME_REQUIRED))
                .andExpect(jsonPath(ERROR_SEVERITY_JSON_PATH).value(MessageSeverity.ERROR.name()));

        assertEquals(0, shoppingListRepository.count());
        assertEquals(0, sharedAccessRepository.count());
        assertEquals(0, sharedAccessMemberRepository.count());
    }

    @Test
    void sameOwnerCannotCreateTwoShoppingListsWithSameName() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(owner.getEmail(), null, ROLE_USER);

        createShoppingList(TESZT, authenticationToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty());

        createShoppingList(TESZT, authenticationToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath(DATA_JSON_PATH).doesNotExist())
                .andExpect(jsonPath(ERROR_TEXT_JSON_PATH).value(SHARED_ACCESS_EXISTS))
                .andExpect(jsonPath(ERROR_SEVERITY_JSON_PATH).value(MessageSeverity.ERROR.name()));

        assertEquals(1, shoppingListRepository.count());
        assertEquals(1, sharedAccessRepository.count());
    }

    @Test
    void sameOwnerCannotCreateShoppingListsWithSameNameIgnoringCase() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(owner.getEmail(), null, ROLE_USER);

        createShoppingList(TESZT_TITLE_CASE, authenticationToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty());

        createShoppingList(TESZT, authenticationToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath(DATA_JSON_PATH).doesNotExist())
                .andExpect(jsonPath(ERROR_TEXT_JSON_PATH).value(SHARED_ACCESS_EXISTS))
                .andExpect(jsonPath(ERROR_SEVERITY_JSON_PATH).value(MessageSeverity.ERROR.name()));

        assertEquals(1, shoppingListRepository.count());
        assertEquals(1, sharedAccessRepository.count());
    }

    @Test
    void sameOwnerCannotCreateShoppingListsWithSameNameIgnoringSurroundingWhitespace() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(owner.getEmail(), null, ROLE_USER);

        createShoppingList(PADDED_TESZT, authenticationToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty())
                .andExpect(jsonPath(DATA_NAME_JSON_PATH).value(TESZT));

        createShoppingList(TESZT, authenticationToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath(DATA_JSON_PATH).doesNotExist())
                .andExpect(jsonPath(ERROR_TEXT_JSON_PATH).value(SHARED_ACCESS_EXISTS))
                .andExpect(jsonPath(ERROR_SEVERITY_JSON_PATH).value(MessageSeverity.ERROR.name()));

        assertEquals(1, shoppingListRepository.count());
        assertEquals(1, sharedAccessRepository.count());
    }

    @Test
    void sameOwnerCanCreateSameNameWithDifferentType() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));

        sharedAccessService.create(owner, SharedAccessType.RECIPE, TESZT);

        createShoppingList(TESZT, new TestingAuthenticationToken(owner.getEmail(), null, ROLE_USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty());

        assertEquals(1, shoppingListRepository.count());
        assertEquals(2, sharedAccessRepository.count());
    }

    @Test
    void differentOwnersCanCreateShoppingListsWithSameName() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        User outsider = userRepository.save(user(OUTSIDER_EMAIL));

        createShoppingList(TESZT, new TestingAuthenticationToken(owner.getEmail(), null, ROLE_USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty());

        createShoppingList(TESZT, new TestingAuthenticationToken(outsider.getEmail(), null, ROLE_USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty());

        assertEquals(2, shoppingListRepository.count());
        assertEquals(2, sharedAccessRepository.count());
    }

    @Test
    void memberCanCreateOwnShoppingListWithSameNameAsSharedList() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        User member = userRepository.save(user(MEMBER_EMAIL));
        SharedAccess ownerSharedAccess = sharedAccessRepository.save(sharedAccess(TESZT, owner));
        shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(ownerSharedAccess)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(ownerSharedAccess)
                .user(owner)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(ownerSharedAccess)
                .user(member)
                .build());

        createShoppingList(TESZT, new TestingAuthenticationToken(member.getEmail(), null, ROLE_USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty());

        assertEquals(2, shoppingListRepository.count());
        assertEquals(2, sharedAccessRepository.count());
    }

    @Test
    void aliasDoesNotAffectCanonicalShoppingListUniqueness() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(owner.getEmail(), null, ROLE_USER);

        createShoppingList(WEEKLY_GROCERIES, authenticationToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty());
        SharedAccess existingSharedAccess = sharedAccessRepository.findAll().get(0);

        sharedAccessAliasRepository.save(SharedAccessAlias.builder()
                .sharedAccess(existingSharedAccess)
                .user(owner)
                .alias(TESZT)
                .build());

        createShoppingList(TESZT, authenticationToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty());

        assertEquals(2, shoppingListRepository.count());
        assertEquals(2, sharedAccessRepository.count());
        assertEquals(1, sharedAccessAliasRepository.count());
    }

    @Test
    void findAllReturnsOnlyAuthenticatedUsersShoppingLists() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        User outsider = userRepository.save(user(OUTSIDER_EMAIL));
        SharedAccess ownerSharedAccess = sharedAccessRepository.save(sharedAccess(OWNER_GROCERIES, owner));
        SharedAccess ownerSecondSharedAccess = sharedAccessRepository.save(sharedAccess(OWNER_PARTY, owner));
        SharedAccess outsiderSharedAccess = sharedAccessRepository.save(sharedAccess(OUTSIDER_GROCERIES, outsider));
        ShoppingList ownerShoppingList = shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(ownerSharedAccess)
                .build());
        ShoppingList ownerSecondShoppingList = shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(ownerSecondSharedAccess)
                .build());
        shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(outsiderSharedAccess)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(ownerSharedAccess)
                .user(owner)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(ownerSecondSharedAccess)
                .user(owner)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(outsiderSharedAccess)
                .user(outsider)
                .build());

        mockMvc.perform(get(API_SHOPPING_LIST_PATH)
                        .with(authentication(new TestingAuthenticationToken(owner.getEmail(), null, ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[?(@.id == %s)]", ownerShoppingList.getId()).exists())
                .andExpect(jsonPath("$.data[?(@.id == %s)]", ownerSecondShoppingList.getId()).exists())
                .andExpect(jsonPath("$.data[?(@.name == '%s')]", OWNER_GROCERIES).exists())
                .andExpect(jsonPath("$.data[?(@.name == '%s')]", OWNER_PARTY).exists())
                .andExpect(jsonPath("$.data[?(@.name == '%s')]", OUTSIDER_GROCERIES).doesNotExist());
    }

    @Test
    void findAllReturnsEmptyListWhenAuthenticatedUserHasNoShoppingLists() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));

        mockMvc.perform(get(API_SHOPPING_LIST_PATH)
                        .with(authentication(new TestingAuthenticationToken(owner.getEmail(), null, ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty())
                .andExpect(jsonPath(DATA_JSON_PATH).isArray())
                .andExpect(jsonPath(DATA_JSON_PATH).isEmpty());
    }

    @Test
    void findByIdReturnsShoppingListWhenListExistsAndUserHasAccess() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        SharedAccess sharedAccess = sharedAccessRepository.save(sharedAccess(OWNER_GROCERIES, owner));
        ShoppingList shoppingList = shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(sharedAccess)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(owner)
                .build());

        mockMvc.perform(get(API_SHOPPING_LIST_PATH + SLASH + shoppingList.getId())
                        .with(authentication(new TestingAuthenticationToken(owner.getEmail(), null, ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty())
                .andExpect(jsonPath(DATA_ID_JSON_PATH).value(shoppingList.getId()))
                .andExpect(jsonPath(DATA_SHARED_ACCESS_ID_JSON_PATH).value(sharedAccess.getId()))
                .andExpect(jsonPath(DATA_NAME_JSON_PATH).value(OWNER_GROCERIES));
    }

    @Test
    void memberCanAccessShoppingList() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        User member = userRepository.save(user(MEMBER_EMAIL));
        SharedAccess sharedAccess = sharedAccessRepository.save(sharedAccess(OWNER_GROCERIES, owner));
        ShoppingList shoppingList = shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(sharedAccess)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(owner)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(member)
                .build());

        mockMvc.perform(get(API_SHOPPING_LIST_PATH + SLASH + shoppingList.getId())
                        .with(authentication(new TestingAuthenticationToken(member.getEmail(), null, ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty())
                .andExpect(jsonPath(DATA_ID_JSON_PATH).value(shoppingList.getId()))
                .andExpect(jsonPath(DATA_SHARED_ACCESS_ID_JSON_PATH).value(sharedAccess.getId()))
                .andExpect(jsonPath(DATA_NAME_JSON_PATH).value(OWNER_GROCERIES));
    }

    @Test
    void findByIdReturnsRestResponseWithErrorMessageWhenShoppingListDoesNotExist() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));

        mockMvc.perform(get(API_SHOPPING_LIST_PATH + "/999")
                        .with(authentication(new TestingAuthenticationToken(owner.getEmail(), null, ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DATA_JSON_PATH).doesNotExist())
                .andExpect(jsonPath(ERROR_TEXT_JSON_PATH).value(SHOPPING_LIST_NOT_FOUND))
                .andExpect(jsonPath(ERROR_SEVERITY_JSON_PATH).value(MessageSeverity.ERROR.name()));
    }

    @Test
    void findByIdReturnsRestResponseWithErrorMessageWhenUserIsNotMember() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        User outsider = userRepository.save(user(OUTSIDER_EMAIL));
        SharedAccess sharedAccess = sharedAccessRepository.save(sharedAccess(OWNER_GROCERIES, owner));
        ShoppingList shoppingList = shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(sharedAccess)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(owner)
                .build());

        mockMvc.perform(get(API_SHOPPING_LIST_PATH + "/" + shoppingList.getId())
                        .with(authentication(new TestingAuthenticationToken(outsider.getEmail(), null, ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DATA_JSON_PATH).doesNotExist())
                .andExpect(jsonPath(ERROR_TEXT_JSON_PATH).value(SHOPPING_LIST_ACCESS_DENIED))
                .andExpect(jsonPath(ERROR_SEVERITY_JSON_PATH).value(MessageSeverity.ERROR.name()));
    }

    @Test
    void ownerCanDeleteShoppingListAndRelatedAliasesAndMembersAreDeleted() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        User member = userRepository.save(user(MEMBER_EMAIL));
        SharedAccess sharedAccess = sharedAccessRepository.save(sharedAccess(WEEKLY_GROCERIES, owner));
        ShoppingList shoppingList = shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(sharedAccess)
                .build());
        Unit unit = unitRepository.findByName(PIECE_UNIT).orElseThrow();

        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(owner)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(member)
                .build());
        sharedAccessAliasRepository.save(SharedAccessAlias.builder()
                .sharedAccess(sharedAccess)
                .user(owner)
                .alias(HOME_ALIAS)
                .build());
        sharedAccessAliasRepository.save(SharedAccessAlias.builder()
                .sharedAccess(sharedAccess)
                .user(member)
                .alias(FLAT_ALIAS)
                .build());
        ShoppingItem shoppingItem = shoppingItemRepository.save(ShoppingItem.builder()
                .shoppingList(shoppingList)
                .name(MILK)
                .quantity(BigDecimal.ONE)
                .unit(unit)
                .build());

        mockMvc.perform(delete(API_SHOPPING_LIST_PATH + SLASH + shoppingList.getId())
                        .with(authentication(new TestingAuthenticationToken(OWNER_EMAIL, null, ROLE_USER))))
                .andExpect(status().isOk());

        assertFalse(shoppingItemRepository.existsById(shoppingItem.getId()));
        assertFalse(shoppingListRepository.existsById(shoppingList.getId()));
        assertFalse(sharedAccessRepository.existsById(sharedAccess.getId()));
        assertEquals(0, shoppingItemRepository.count());
        assertEquals(0, sharedAccessMemberRepository.count());
        assertEquals(0, sharedAccessAliasRepository.count());
    }

    @Test
    void memberCanLeaveShoppingListAndOwnAliasIsDeleted() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        User member = userRepository.save(user(MEMBER_EMAIL));
        SharedAccess sharedAccess = sharedAccessRepository.save(sharedAccess(WEEKLY_GROCERIES, owner));
        ShoppingList shoppingList = shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(sharedAccess)
                .build());

        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(owner)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(member)
                .build());
        sharedAccessAliasRepository.save(SharedAccessAlias.builder()
                .sharedAccess(sharedAccess)
                .user(owner)
                .alias(HOME_ALIAS)
                .build());
        sharedAccessAliasRepository.save(SharedAccessAlias.builder()
                .sharedAccess(sharedAccess)
                .user(member)
                .alias(FLAT_ALIAS)
                .build());

        mockMvc.perform(delete(API_SHOPPING_LIST_PATH + SLASH + shoppingList.getId() + MEMBERS_ME)
                        .with(authentication(new TestingAuthenticationToken(MEMBER_EMAIL, null, ROLE_USER))))
                .andExpect(status().isOk());

        assertEquals(1, shoppingListRepository.count());
        assertEquals(1, sharedAccessRepository.count());
        assertFalse(sharedAccessMemberRepository.existsBySharedAccessIdAndUserId(sharedAccess.getId(), member.getId()));
        assertEquals(1, sharedAccessMemberRepository.count());
        assertEquals(owner.getId(), sharedAccessMemberRepository.findAll().get(0).getUser().getId());
        assertEquals(1, sharedAccessAliasRepository.count());
        assertEquals(owner.getId(), sharedAccessAliasRepository.findAll().get(0).getUser().getId());
    }

    @Test
    void ownerLeavingDeletesShoppingListAndSharedAccess() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        SharedAccess sharedAccess = sharedAccessRepository.save(sharedAccess(WEEKLY_GROCERIES, owner));
        ShoppingList shoppingList = shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(sharedAccess)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(owner)
                .build());

        mockMvc.perform(delete(API_SHOPPING_LIST_PATH + SLASH + shoppingList.getId() + MEMBERS_ME)
                        .with(authentication(new TestingAuthenticationToken(OWNER_EMAIL, null, ROLE_USER))))
                .andExpect(status().isOk());

        assertFalse(shoppingListRepository.existsById(shoppingList.getId()));
        assertFalse(sharedAccessRepository.existsById(sharedAccess.getId()));
        assertEquals(0, sharedAccessMemberRepository.count());
    }

    @Test
    void findAllListsDerivesRolesFromSharedAccessOwner() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        User member = userRepository.save(user(MEMBER_EMAIL));
        SharedAccess sharedAccess = sharedAccessRepository.save(sharedAccess(WEEKLY_GROCERIES, owner));
        shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(sharedAccess)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(owner)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(member)
                .build());

        mockMvc.perform(get(API_LIST_PATH)
                        .with(authentication(new TestingAuthenticationToken(OWNER_EMAIL, null, ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty())
                .andExpect(jsonPath("$.data[0].role").value("OWNER"))
                .andExpect(jsonPath("$.data[0].isOwner").value(true))
                .andExpect(jsonPath("$.data[0].members[?(@.userId == %s && @.role == 'OWNER')]", owner.getId()).exists())
                .andExpect(jsonPath("$.data[0].members[?(@.userId == %s && @.role == 'MEMBER')]", member.getId()).exists());

        mockMvc.perform(get(API_LIST_PATH)
                        .with(authentication(new TestingAuthenticationToken(MEMBER_EMAIL, null, ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGES_JSON_PATH).isEmpty())
                .andExpect(jsonPath("$.data[0].role").value("MEMBER"))
                .andExpect(jsonPath("$.data[0].isOwner").value(false));
    }

    @Test
    void sharedAccessMemberDoesNotStoreRole() {
        Assertions.assertThrows(NoSuchFieldException.class, () -> SharedAccessMember.class.getDeclaredField("role"));
    }

    @Test
    void nonMemberCannotDeleteShoppingListAndGetsRestResponseWithErrorMessage() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        User outsider = userRepository.save(user(OUTSIDER_EMAIL));
        SharedAccess sharedAccess = sharedAccessRepository.save(sharedAccess(WEEKLY_GROCERIES, owner));
        ShoppingList shoppingList = shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(sharedAccess)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(owner)
                .build());

        mockMvc.perform(delete(API_SHOPPING_LIST_PATH + SLASH + shoppingList.getId())
                        .with(authentication(new TestingAuthenticationToken(outsider.getEmail(), null, ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DATA_JSON_PATH).doesNotExist())
                .andExpect(jsonPath(ERROR_TEXT_JSON_PATH).value(SHOPPING_LIST_ACCESS_DENIED))
                .andExpect(jsonPath(ERROR_SEVERITY_JSON_PATH).value(MessageSeverity.ERROR.name()));

        assertEquals(1, shoppingListRepository.count());
        assertEquals(1, sharedAccessRepository.count());
        assertEquals(1, sharedAccessMemberRepository.count());
    }

    @Test
    void memberCannotDeleteShoppingListAndGetsRestResponseWithErrorMessage() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        User member = userRepository.save(user(MEMBER_EMAIL));
        SharedAccess sharedAccess = sharedAccessRepository.save(sharedAccess(WEEKLY_GROCERIES, owner));
        ShoppingList shoppingList = shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(sharedAccess)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(owner)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(member)
                .build());

        mockMvc.perform(delete(API_SHOPPING_LIST_PATH + SLASH + shoppingList.getId())
                        .with(authentication(new TestingAuthenticationToken(member.getEmail(), null, ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DATA_JSON_PATH).doesNotExist())
                .andExpect(jsonPath(ERROR_TEXT_JSON_PATH).value(SHOPPING_LIST_ACCESS_DENIED))
                .andExpect(jsonPath(ERROR_SEVERITY_JSON_PATH).value(MessageSeverity.ERROR.name()));

        assertEquals(1, shoppingListRepository.count());
        assertEquals(1, sharedAccessRepository.count());
        assertEquals(2, sharedAccessMemberRepository.count());
    }

    @Test
    void nonMemberCannotLeaveShoppingListAndGetsRestResponseWithErrorMessage() throws Exception {
        User owner = userRepository.save(user(OWNER_EMAIL));
        User outsider = userRepository.save(user(OUTSIDER_EMAIL));
        SharedAccess sharedAccess = sharedAccessRepository.save(sharedAccess(WEEKLY_GROCERIES, owner));
        ShoppingList shoppingList = shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(sharedAccess)
                .build());
        sharedAccessMemberRepository.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(owner)
                .build());

        mockMvc.perform(delete(API_SHOPPING_LIST_PATH + SLASH + shoppingList.getId() + MEMBERS_ME)
                        .with(authentication(new TestingAuthenticationToken(outsider.getEmail(), null, ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DATA_JSON_PATH).doesNotExist())
                .andExpect(jsonPath(ERROR_TEXT_JSON_PATH).value(SHOPPING_LIST_ACCESS_DENIED))
                .andExpect(jsonPath(ERROR_SEVERITY_JSON_PATH).value(MessageSeverity.ERROR.name()));

        assertEquals(1, shoppingListRepository.count());
        assertEquals(1, sharedAccessRepository.count());
        assertEquals(1, sharedAccessMemberRepository.count());
    }

    @Test
    void shoppingItemBelongsToShoppingList() {
        User owner = userRepository.save(user(OWNER_EMAIL));
        SharedAccess sharedAccess = sharedAccessRepository.save(sharedAccess(WEEKLY_GROCERIES, owner));
        ShoppingList shoppingList = shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(sharedAccess)
                .build());
        Unit unit = unitRepository.findByName(PIECE_UNIT).orElseThrow();

        ShoppingItem shoppingItem = shoppingItemRepository.save(ShoppingItem.builder()
                .shoppingList(shoppingList)
                .name(MILK)
                .quantity(BigDecimal.ONE)
                .unit(unit)
                .build());

        assertEquals(1, shoppingItemRepository.count());
        assertEquals(shoppingList.getId(), shoppingItem.getShoppingList().getId());
        assertEquals(MILK, shoppingItem.getName());
        assertEquals(unit.getId(), shoppingItem.getUnit().getId());
    }

    @Test
    void shoppingItemCannotExistWithoutShoppingList() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> shoppingItemRepository.saveAndFlush(
                ShoppingItem.builder()
                        .name(MILK)
                        .quantity(BigDecimal.ONE)
                        .build()
        ));
    }

    private ResultActions createShoppingList(String name, TestingAuthenticationToken authenticationToken) throws Exception {
        return mockMvc.perform(post(API_SHOPPING_LIST_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\"}")
                .with(authentication(authenticationToken)));
    }

    private SharedAccess sharedAccess(String name, User owner) {
        String trimmedName = name.trim();
        return SharedAccess.builder()
                .name(trimmedName)
                .ownerUser(owner)
                .type(SharedAccessType.SHOPPING)
                .canonicalName(canonicalizeName(name))
                .build();
    }

    private String canonicalizeName(String name) {
        return Normalizer.normalize(name.trim(), Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT);
    }

    private User user(String email) {
        Instant now = Instant.now();
        User user = new User();
        user.setEmail(email);
        user.setName(email);
        user.setRole(UserRole.USER);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return user;
    }
}
