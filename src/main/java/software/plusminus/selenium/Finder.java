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
import org.openqa.selenium.SearchContext;
import software.plusminus.selenium.exception.SeleniumException;
import software.plusminus.selenium.model.Range;
import software.plusminus.selenium.model.Visibility;

import java.util.List;

@SuppressWarnings("HiddenField")
public class Finder {

    private final Selenium selenium;

    private SearchContext context;
    private By by;
    private Visibility visibility = Visibility.ALL;

    public Finder(Selenium selenium) {
        this.selenium = selenium;
        this.context = selenium.driver();
    }

    public List<Element> all(int size) {
        return all(new Range(size, size));
    }

    public List<Element> all(Range size) {
        if (by == null) {
            throw new SeleniumException("Incorrect 'by' parameter: must be non null");
        }
        return selenium.findAll(context, by, size, visibility);
    }

    public List<Element> min(int minSize) {
        return all(new Range(minSize, Integer.MAX_VALUE));
    }

    public List<Element> max(int maxSize) {
        return all(new Range(0, maxSize));
    }

    public List<Element> atLeastOne() {
        return min(1);
    }

    public Element one() {
        return all(1).get(0);
    }

    public void none() {
        all(0);
    }

    public Finder context(SearchContext context) {
        this.context = context;
        return this;
    }

    public Finder by(By by) {
        if (this.by != null) {
            throw new SeleniumException("The 'by' parameter has been defined already");
        }
        this.by = by;
        return this;
    }

    public Finder bySelector(String selector) {
        return by(By.cssSelector(selector));
    }

    public Finder byText(String htmlTag, String text) {
        return by(Findable.byText(htmlTag, text));
    }

    public Finder displayed() {
        this.visibility = Visibility.DISPLAYED;
        return this;
    }

    public Finder hidden() {
        this.visibility = Visibility.HIDDEN;
        return this;
    }

}
