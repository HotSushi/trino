/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.sql.planner.iterative.rule;

import com.google.common.collect.ImmutableList;
import io.trino.sql.planner.Symbol;
import io.trino.sql.planner.iterative.rule.test.BaseRuleTest;
import io.trino.sql.planner.plan.TableFunctionNode.PassThroughColumn;
import io.trino.sql.planner.plan.TableFunctionNode.PassThroughSpecification;
import org.junit.jupiter.api.Test;

import static io.trino.sql.planner.assertions.PlanMatchPattern.values;

public class TestRemoveRedundantTableFunction
        extends BaseRuleTest
{
    @Test
    public void testRemoveTableFunction()
    {
        tester().assertThat(new RemoveRedundantTableFunction())
                .on(p -> {
                    Symbol passThrough = p.symbol("pass_through");
                    Symbol proper = p.symbol("proper");
                    return p.tableFunctionProcessor(
                            builder -> builder
                                    .name("test_function")
                                    .pruneWhenEmpty()
                                    .properOutputs(proper)
                                    .passThroughSpecifications(new PassThroughSpecification(false, ImmutableList.of(new PassThroughColumn(passThrough, true))))
                                    .source(p.values(passThrough)));
                })
                .matches(values("proper", "pass_through"));
    }

    @Test
    public void testDoNotRemoveKeepWhenEmpty()
    {
        tester().assertThat(new RemoveRedundantTableFunction())
                .on(p -> {
                    Symbol passThrough = p.symbol("pass_through");
                    Symbol proper = p.symbol("proper");
                    return p.tableFunctionProcessor(
                            builder -> builder
                                    .name("test_function")
                                    .properOutputs(proper)
                                    .passThroughSpecifications(new PassThroughSpecification(false, ImmutableList.of(new PassThroughColumn(passThrough, true))))
                                    .source(p.values(passThrough)));
                })
                .doesNotFire();
    }

    @Test
    public void testDoNotRemoveNonEmptyInput()
    {
        tester().assertThat(new RemoveRedundantTableFunction())
                .on(p -> {
                    Symbol passThrough = p.symbol("pass_through");
                    Symbol proper = p.symbol("proper");
                    return p.tableFunctionProcessor(
                            builder -> builder
                                    .name("test_function")
                                    .pruneWhenEmpty()
                                    .properOutputs(proper)
                                    .passThroughSpecifications(new PassThroughSpecification(false, ImmutableList.of(new PassThroughColumn(passThrough, true))))
                                    .source(p.values(5, passThrough)));
                })
                .doesNotFire();
    }
}
