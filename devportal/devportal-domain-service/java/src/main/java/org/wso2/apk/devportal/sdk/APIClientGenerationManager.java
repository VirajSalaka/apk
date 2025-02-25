/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.devportal.sdk;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;
import org.wso2.apk.devportal.sdk.SDKConstants;
import org.wso2.apk.devportal.sdk.ZIPUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

/*
 * This class is used to generate SDKs for a given API
 */
public class APIClientGenerationManager {

    private static final Log log = LogFactory.getLog(APIClientGenerationManager.class);
    private static final Map<String, String> langCodeGen = new HashMap<String, String>();

    public APIClientGenerationManager() {
        langCodeGen.put("java", "org.openapitools.codegen.languages.JavaClientCodegen");
        langCodeGen.put("android", "org.openapitools.codegen.languages.AndroidClientCodegen");
        langCodeGen.put("csharp", "org.openapitools.codegen.languages.CSharpClientCodegen");
        langCodeGen.put("cpp", "org.openapitools.codegen.languages.CppRestClientCodegen");
        langCodeGen.put("dart", "org.openapitools.codegen.languages.DartClientCodegen");
        langCodeGen.put("flash", "org.openapitools.codegen.languages.FlashClientCodegen");
        langCodeGen.put("go", "org.openapitools.codegen.languages.GoClientCodegen");
        langCodeGen.put("groovy", "org.openapitools.codegen.languages.GroovyClientCodegen");
        langCodeGen.put("javascript", "org.openapitools.codegen.languages.JavascriptClientCodegen");
        langCodeGen.put("jmeter", "org.openapitools.codegen.languages.JMeterCodegen");
        langCodeGen.put("nodejs", "org.openapitools.codegen.languages.NodeJSServerCodegen");
        langCodeGen.put("perl", "org.openapitools.codegen.languages.PerlClientCodegen");
        langCodeGen.put("php", "org.openapitools.codegen.languages.PhpClientCodegen");
        langCodeGen.put("python", "org.openapitools.codegen.languages.PythonClientCodegen");
        langCodeGen.put("ruby", "org.openapitools.codegen.languages.RubyClientCodegen");
        langCodeGen.put("swift", "org.openapitools.codegen.languages.SwiftCodegen");
        langCodeGen.put("clojure", "org.openapitools.codegen.languages.ClojureClientCodegen");
        langCodeGen.put("aspNet5", "org.openapitools.codegen.languages.AspNet5ServerCodegen");
        langCodeGen.put("scala-akka-client", "org.openapitools.codegen.languages.ScalaAkkaClientCodegen");
        langCodeGen.put("spring", "org.openapitools.codegen.languages.SpringCodegen");
        langCodeGen.put("csharpDotNet2", "org.openapitools.codegen.languages.CsharpDotNet2ClientCodegen");
        langCodeGen.put("haskell", "org.openapitools.codegen.languages.HaskellServantCodegen");
    }

    /**
     * This method generates client side SDK for a given API
     *
     * @param sdkLanguage          preferred language to generate the SDK
     * @param apiName              name of the API
     * @param apiVersion           version of the API
     * @param swaggerAPIDefinition Swagger Definition of the API
     * @param groupId              group id of the generated SDK
     * @param artifactId           artifact id of the generated SDK
     * @param modelPackage         model package name of the generated SDK
     * @param apiPackage           api package name of the generated SDK
     * @return a map containing the zip file name and its' temporary location until it is downloaded
     * @throws APIClientGenerationException if failed to generate the SDK
     */
    public Map<String, String> generateSDK(String sdkLanguage, String apiName, String apiVersion,
                                           String swaggerAPIDefinition, String groupId, String artifactId,
                                           String modelPackage, String apiPackage)
            throws APIClientGenerationException {

        if (StringUtils.isBlank(sdkLanguage) || StringUtils.isBlank(apiName) || StringUtils.isBlank(apiVersion)) {
            handleSDKGenException("SDK Language, API Name or API Version should not be null.");
        }
        if (StringUtils.isEmpty(swaggerAPIDefinition)) {
            handleSDKGenException("Error loading the Swagger definition. Swagger file is empty.");
        }
        //create a temporary directory with a random name to store files created during generating the SDK
        Path path = Paths.get(FileUtils.getTempDirectory().getAbsolutePath(), UUID.randomUUID().toString());
        String tempDirectoryLocation = path.toFile().getAbsolutePath();
        try {
            tempDirectoryLocation = Files.createDirectories(path).toFile().getAbsolutePath();
        } catch (IOException e) {
            handleSDKGenException("Unable to create temporary directory in : " + tempDirectoryLocation);
        }
        String specFileLocation = tempDirectoryLocation + File.separator + UUID.randomUUID() +
                SDKConstants.JSON_FILE_EXTENSION;
        //The below swaggerSpecFile will be deleted when cleaning the temp directory by the caller
        try {
            File swaggerSpecFile = new File(specFileLocation);

            boolean isSpecFileCreated = swaggerSpecFile.createNewFile();
            if (!isSpecFileCreated) {
                handleSDKGenException("Unable to create the swagger spec file for API : " + apiName + " in " +
                        specFileLocation);
            }
            try (FileWriter fileWriter = new FileWriter(swaggerSpecFile.getAbsoluteFile())) {
                try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                    bufferedWriter.write(swaggerAPIDefinition);
                }
            }
        } catch (IOException e) {
            handleSDKGenException("Error while storing the temporary swagger file in : " + specFileLocation, e);
        }

        String sdkDirectoryName = apiName + "_" + apiVersion + "_" + sdkLanguage;
        String temporaryOutputPath = tempDirectoryLocation + File.separator + sdkDirectoryName;
        generateClient(apiName, specFileLocation, sdkLanguage, temporaryOutputPath,
                groupId, artifactId, modelPackage, apiPackage);
        String temporaryZipFilePath = temporaryOutputPath + SDKConstants.ZIP_FILE_EXTENSION;
        try {
            ZIPUtils.zipDir(temporaryOutputPath, temporaryZipFilePath);
        } catch (IOException e) {
            handleSDKGenException("Error while generating .zip archive for the generated SDK.", e);
        }
        //The below file object is closed and deleted by the caller, so it should left open until the SDK is downloaded.
        File sdkArchive = new File(temporaryZipFilePath);
        Map<String, String> sdkDataMap = new HashMap<String, String>();
        sdkDataMap.put("zipFilePath", sdkArchive.getAbsolutePath());
        sdkDataMap.put("zipFileName", sdkDirectoryName + SDKConstants.ZIP_FILE_EXTENSION);
        sdkDataMap.put("tempDirectoryPath", tempDirectoryLocation);
        return sdkDataMap;
    }

    public void cleanTempDirectory(String tempDirectoryPath) {
        if (StringUtils.isNotBlank(tempDirectoryPath)) {
            try {
                FileUtils.cleanDirectory(new File(tempDirectoryPath));
            } catch (IOException e) {
                // Ignore this exception since this temp directory is automatically deleted after a server
                // restart. These temp files can be manually deleted if needed. Reference : APIMANAGER-4981
                log.warn("Failed to clean temporary files at : " + tempDirectoryPath +
                        " Delete those files manually or it will be cleared after a server restart.");
            }
        }
    }

    /**
     * This method is used to retrieve the supported languages for SDK generation
     *
     * @return supported languages for SDK generation
     */
    public String getSupportedSDKLanguages() {
        String supportedLanguages = SDKConstants.CLIENT_CODEGEN_SUPPORTED_LANGUAGES;
        return supportedLanguages;

    }

    /**
     * This method is used to generate SDK for a API for a given language
     *
     * @param apiName             name of the API
     * @param specLocation        location of the swagger spec for the API
     * @param sdkLanguage         preferred SDK language
     * @param temporaryOutputPath temporary location where the SDK archive is saved until downloaded
     */
    private void generateClient(String apiName, String specLocation, String sdkLanguage, String temporaryOutputPath,
                                String groupId, String artifactId, String modelPackage, String apiPackage) {

        CodegenConfigurator codegenConfigurator = new CodegenConfigurator();
        codegenConfigurator.setGroupId(groupId);
        codegenConfigurator.setArtifactId(artifactId + apiName);
        codegenConfigurator
                .setModelPackage(modelPackage + apiName);
        codegenConfigurator.setApiPackage(apiPackage + apiName);
        codegenConfigurator.setInputSpec(specLocation);
        codegenConfigurator.setGeneratorName(sdkLanguage);
        codegenConfigurator.setOutputDir(temporaryOutputPath);
        codegenConfigurator.setValidateSpec(false);
        final ClientOptInput clientOptInput = codegenConfigurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

    }

    /**
     * This method is to handle exceptions occurred when generating the SDK
     *
     * @param errorMessage error message to be printed in the log
     * @throws APIClientGenerationException
     */
    private void handleSDKGenException(String errorMessage) throws APIClientGenerationException {
        log.error(errorMessage);
        throw new APIClientGenerationException(errorMessage);
    }

    /**
     * This method is to handle exceptions occurred when generating the SDK (with a throwable exception)
     *
     * @param errorMessage error message to be printed in the log
     * @param throwable    throwable exception caught
     * @throws APIClientGenerationException
     */
    private void handleSDKGenException(String errorMessage, Throwable throwable) throws APIClientGenerationException {
        log.error(errorMessage, throwable);
        throw new APIClientGenerationException(errorMessage, throwable);
    }
}
