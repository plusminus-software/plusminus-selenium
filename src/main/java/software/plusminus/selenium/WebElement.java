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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_INTERFACE")
@SuppressWarnings("squid:S2176")
@RequiredArgsConstructor(staticName = "of")
public class WebElement implements org.openqa.selenium.WebElement, Findable {

    @Delegate
    private final org.openqa.selenium.WebElement canonical;
    private final Selenium selenium;

    public Finder find() {
        Finder finder = new Finder(selenium);
        finder.context(this);
        return finder;
    }

    public org.openqa.selenium.WebElement getCanonicalElement() {
        return canonical;
    }

    public WebElement getParent() {
        return WebElement.of(findElement(Findable.byParent()), selenium);
    }

    public String getValue() {
        return getAttribute("value");
    }
}
