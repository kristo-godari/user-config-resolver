package org.godari.config.resolver;

import org.godari.config.resolver.api.ConfigResolverException;
import org.godari.config.resolver.config.JunitConfig;
import org.godari.config.resolver.dto.TestDto;
import org.godari.config.resolver.service.JsonConfigResolverService;
import org.godari.config.resolver.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(JunitConfig.class)
public class JsonConfigResolverServiceTest {

    @Autowired
    JsonConfigResolverService jsonConfigResolverService;

    static Stream<Arguments> generateTestData() {
        return Stream.of(
                Arguments.of(new HashSet<>(List.of("group-a", "group-b")), "user-in-all-groups/input.json", "user-in-all-groups/output.json"),
                Arguments.of(new HashSet<>(List.of("group-d")), "user-in-any-groups/input.json", "user-in-any-groups/output.json"),
                Arguments.of(new HashSet<>(List.of("group-c")), "user-in-no-groups/input.json", "user-in-no-groups/output.json"),
                Arguments.of(new HashSet<>(List.of("group-a", "group-b", "group-c")), "user-in-different-groups/input.json", "user-in-different-groups/output.json"),
                Arguments.of(new HashSet<>(List.of("group-a", "group-b", "group-c")), "custom-user-groups/input.json", "custom-user-groups/output.json")
        );
    }

    @ParameterizedTest
    @MethodSource("generateTestData")
    public void userIsInAllGroups_resolveConfig_configResolvedCorrectlyAsObject(
            Set<String> userGroups, String inputFilePath, String outputFilePath
    ) throws ConfigResolverException, IOException {

        // given
        String inputConfig = TestUtils.readFile(inputFilePath);
        TestDto outputConfig = TestUtils.readFileIntoObject(outputFilePath, TestDto.class);

        // when
        TestDto resolvedConfig = jsonConfigResolverService.resolveConfig(inputConfig, userGroups, TestDto.class);

        // then
        assertEquals(outputConfig, resolvedConfig);

    }

    @ParameterizedTest
    @MethodSource("generateTestData")
    public void userIsInAllGroups_resolveConfig_configResolvedCorrectlyAsString(
            Set<String> userGroups, String inputFilePath, String outputFilePath
    ) throws ConfigResolverException, IOException {

        // given
        String inputConfig = TestUtils.readFile(inputFilePath);
        String outputConfig = TestUtils.readFile(outputFilePath).replaceAll("\\s", "");

        // when
        String resolvedConfig = jsonConfigResolverService.resolveConfig(inputConfig, userGroups);

        // then
        assertEquals(outputConfig, resolvedConfig);

    }

    @ParameterizedTest
    @MethodSource("generateTestData")
    public void userIsInAllGroups_resolveConfigCalledWithoutInputConfig_configResolvedCorrectlyAsObject(
            Set<String> userGroups, String inputFilePath, String outputFilePath
    ) throws ConfigResolverException, IOException {

        // given
        String inputConfig = TestUtils.readFile(inputFilePath);
        TestDto outputConfig = TestUtils.readFileIntoObject(outputFilePath, TestDto.class);
        jsonConfigResolverService.setConfigToResolve(inputConfig);

        // when
        TestDto resolvedConfig = jsonConfigResolverService.resolveConfig(userGroups, TestDto.class);

        // then
        assertEquals(outputConfig, resolvedConfig);

    }

    @ParameterizedTest
    @MethodSource("generateTestData")
    public void userIsInAllGroups_resolveConfigWithoutInputConfig_configResolvedCorrectlyAsString(
            Set<String> userGroups, String inputFilePath, String outputFilePath
    ) throws ConfigResolverException, IOException {

        // given
        String inputConfig = TestUtils.readFile(inputFilePath);
        String outputConfig = TestUtils.readFile(outputFilePath).replaceAll("\\s", "");
        jsonConfigResolverService.setConfigToResolve(inputConfig);

        // when
        String resolvedConfig = jsonConfigResolverService.resolveConfig(userGroups);

        // then
        assertEquals(outputConfig, resolvedConfig);

    }

    @Test
    public void invalidInput_resolveConfigAsObject_exceptionThrown() throws IOException {

        // given
        Set<String> userGroups = Set.of("group-a", "group-b");
        String inputConfig = TestUtils.readFile("invalid-config/input.json");

        // when
        ConfigResolverException exception = assertThrows(ConfigResolverException.class, () -> {
            jsonConfigResolverService.resolveConfig(inputConfig, userGroups, TestDto.class);
        });

        // then
        assertNotNull(exception);

    }

    @Test
    public void invalidInput_resolveConfigAsString_exceptionThrown() throws IOException {

        // given
        Set<String> userGroups = Set.of("group-a", "group-b");
        String inputConfig = TestUtils.readFile("invalid-config/input.json");

        // when
        ConfigResolverException exception = assertThrows(ConfigResolverException.class, () -> {
            jsonConfigResolverService.resolveConfig(inputConfig, userGroups);
        });

        // then
        assertNotNull(exception);

    }

    @Test
    public void invalidInput_resolveConfigAsStringWithoutInputConfig_exceptionThrown() throws IOException {

        // given
        Set<String> userGroups = Set.of("group-a", "group-b");
        String inputConfig = TestUtils.readFile("invalid-config/input.json");
        jsonConfigResolverService.setConfigToResolve(inputConfig);

        // when
        ConfigResolverException exception = assertThrows(ConfigResolverException.class, () -> {
            jsonConfigResolverService.resolveConfig(userGroups);
        });

        // then
        assertNotNull(exception);

    }

    @Test
    public void invalidInput_resolveConfigAsObjectWithoutInputConfig_exceptionThrown() throws IOException {

        // given
        Set<String> userGroups = Set.of("group-a", "group-b");
        String inputConfig = TestUtils.readFile("invalid-config/input.json");
        jsonConfigResolverService.setConfigToResolve(inputConfig);

        // when
        ConfigResolverException exception = assertThrows(ConfigResolverException.class, () -> {
            jsonConfigResolverService.resolveConfig(userGroups, TestDto.class);
        });

        // then
        assertNotNull(exception);
    }
}
