//
// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

import ballerina/uuid;

isolated function addAPICategory(APICategory payload) returns CreatedAPICategory|APKError {
    string org = "carbon.super";
    boolean|APKError existingCategory = checkAPICategoryExistsByNameDAO(payload.name, org);
    if existingCategory is APKError {
        return existingCategory;
    } else if existingCategory is true {
        string message = "API Category already exists by name:" + payload.name;
        return error(message, message = message, description = message, code = 90911, statusCode = "400");
    }
    string categoryId = uuid:createType1AsString();
    payload.id = categoryId;
    APICategory|APKError category = addAPICategoryDAO(payload, org);
    if category is APICategory {
        category.numberOfAPIs = 0;
        CreatedAPICategory createdCategory = {body: category};
        return createdCategory;
    } else {
        return category;
    }
}

isolated function getAllCategoryList() returns APICategoryList|APKError {
    string org = "carbon.super";
    APICategory[]|APKError categories = getAPICategoriesDAO(org);
    if categories is APICategory[] {
        int count = categories.length();
        if (count > 0) {
            foreach APICategory apiCategory in categories {
                //TODO:(Sampath) need to properly retrieve attached api count per category 
                //int count = isCategoryAttached(apiCategory.name);
                int apiCount = 0;
                apiCategory.numberOfAPIs = apiCount;
            }
        }
        APICategoryList apiCategoriesList = {count: count, list: categories};
        return apiCategoriesList;
    } else {
        return categories;
    }
}

isolated function updateAPICategory(string id, APICategory body) returns APICategory|NotFoundError|APKError {
    string org = "carbon.super";
    APICategory|APKError|NotFoundError existingAPICategory = getAPICategoryByIdDAO(id, org);
    if existingAPICategory !is APICategory {
        return existingAPICategory;
    } else {
        body.id = id;
        string existingName = existingAPICategory.name;
        if (existingName != body.name) {
            boolean|APKError existingCategory = checkAPICategoryExistsByNameDAO(body.name, org);
            if existingCategory is APKError {
                return existingCategory;
            }
            //We allow to update API Category name given that the new category name is not taken yet
            if existingCategory is true {
                string message = "API Category already exists by name:"+ body.name;
                APKError e = error(message, message = message, description = message, code = 90911, statusCode = "400");
                return e;
            }
        } 
    }
    APICategory|APKError response = updateAPICategoryDAO(body, org);
    if response is APICategory {
        //TODO:(Sampath) need to properly retrieve attached api count per category 
        //int count = isCategoryAttached(apiCategory.name);
        int apiCount = 0;
        response.numberOfAPIs = apiCount;
    }
    return response;
}

isolated function removeAPICategory(string id) returns string|APKError {
    string org = "carbon.super";
    APKError|string status = deleteAPICategoryDAO(id, org);
    return status;
}
