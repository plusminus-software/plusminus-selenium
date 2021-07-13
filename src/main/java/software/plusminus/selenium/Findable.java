/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.plusminus.selenium;

import org.openqa.selenium.By;
import software.plusminus.selenium.model.Visibility;

import java.util.List;

public interface Findable {

    Finder find();

    default Finder find(String selector) {
        Finder finder = find();
        finder.bySelector(selector);
        return finder;
    }

    default Finder find(By by) {
        Finder finder = find();
        finder.by(by);
        return finder;
    }

    // Find by label

    default Element findByLabel(String label, String selector) {
        Element labelComponent = find().byText("label", label).displayed().one();
        return labelComponent.getParent().find(By.cssSelector(selector)).one();
    }

    default List<Element> findByLabel(String label, String selector, int size) {
        Element labelComponent = find().byText("label", label).displayed().one();
        return labelComponent.getParent().find(By.cssSelector(selector)).all(size);
    }

    // Find all

    default List<Element> findAll(String selector, int size) {
        return findAll(selector, size, Visibility.ALL);
    }

    default List<Element> findAll(String selector, int size, Visibility visibility) {
        return findAll(By.cssSelector(selector), size, visibility);
    }

    default List<Element> findAll(By by, int size, Visibility visibility) {
        Finder finder = find().by(by);
        if (visibility == Visibility.DISPLAYED) {
            finder.displayed();
        } else if (visibility == Visibility.HIDDEN) {
            finder.hidden();
        }
        return finder.all(size);
    }

    // By

    static By byText(String htmlTag, String text) {
        return By.xpath(".//" + htmlTag + "[normalize-space() = '" + text + "']");
    }

    static By byParent() {
        return By.xpath("./..");
    }
}
