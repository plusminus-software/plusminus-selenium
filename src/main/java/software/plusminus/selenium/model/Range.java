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
package software.plusminus.selenium.model;

public class Range {
    private int low;
    private int high;

    public Range(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public boolean between(int number) {
        return number >= low && number <= high;
    }

    @Override
    public String toString() {
        if (low == high) {
            return String.valueOf(low);
        }
        return String.format("[%s, %s]", low, high);
    }
}
