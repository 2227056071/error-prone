/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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

package com.google.errorprone;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

/**
 * Describes a bug pattern detected by error-prone. Used to generate compiler error messages,
 * for @SuppressWarnings, and to generate the documentation that we host on our web site.
 *
 * @author eaftan@google.com (Eddie Aftandilian)
 */
@Retention(RUNTIME)
public @interface BugPattern {

  /** A collection of standardized tags that can be applied to BugPatterns. */
  final class StandardTags {
    private StandardTags() {}

    /**
     * This check, for reasons of backwards compatibility or difficulty in cleaning up, should be
     * considered very likely to represent a real error in the vast majority ({@code >99.9%}) of
     * cases, but couldn't otherwise be turned on as an ERROR.
     *
     * <p>Systems trying to determine the set of likely errors from a collection of BugPatterns
     * should act as if any BugPattern with {@link #severity()} of {@link SeverityLevel#ERROR} also
     * has this tag.
     */
    public static final String LIKELY_ERROR = "LikelyError";

    /**
     * This check detects a coding pattern that is valid within the Java language and doesn't
     * represent a runtime defect, but is otherwise discouraged for reasons of consistency within a
     * project or ease of understanding by other programmers.
     *
     * <p>Checks using this tag should limit their replacements to those that don't change the
     * behavior of the code (for example: adding clarifying parentheses, reordering modifiers in a
     * single declaration, removing implicit modifiers like {@code public} for members in an {@code
     * interface}).
     */
    public static final String STYLE = "Style";

    /**
     * This check detects a potential performance issue, where an easily-identifiable replacement
     * for the code being made will always result in a net positive performance improvement.
     */
    public static final String PERFORMANCE = "Performance";

    /**
     * This check detects code that may technically be working within a limited domain, but is
     * fragile, or violates generally-accepted assumptions of behavior.
     *
     * <p>Examples: DefaultCharset, where code implicitly uses the JVM default charset, will work in
     * circumstances where data being fed to the system happens to be compatible with the Charset,
     * but breaks down if fed data outside.
     */
    public static final String FRAGILE_CODE = "FragileCode";

    /**
     * This check points out potential issues when operating in a concurrent context
     *
     * <p>The code may work fine when accessed by 1 thread at a time, but may have some unintended
     * behavior when running in multiple threads.
     */
    public static final String CONCURRENCY = "Concurrency";

    /**
     * This check points out a coding pattern that, while functional, has an easier-to-read or
     * faster alternative.
     */
    public static final String SIMPLIFICATION = "Simplification";
  }

  /**
   * A unique identifier for this bug, used for @SuppressWarnings and in the compiler error message.
   */
  String name();

  /** Alternate identifiers for this bug, which may also be used in @SuppressWarnings. */
  String[] altNames() default {};

  /** The type of link to generate in the compiler error message. */
  LinkType linkType() default LinkType.AUTOGENERATED;

  /** The link URL to use if linkType() is LinkType.CUSTOM. */
  String link() default "";

  public enum LinkType {
    /** Link to autogenerated documentation, hosted on the error-prone web site. */
    AUTOGENERATED,
    /** Custom string. */
    CUSTOM,
    /** No link should be displayed. */
    NONE
  }

  /**
   * A list of Stringly-typed tags to apply to this check. These tags can be consumed by tools
   * aggregating Error Prone checks (for example: a git pre-commit hook could clean up Java source
   * by finding any checks tagged "Style", run an Error Prone compile over the code with those
   * checks enabled, collect the fixes suggested and apply them).
   *
   * <p>To allow for sharing of tags across systems, a number of standard tags are available as
   * static constants in {@link StandardTags}. It is strongly encouraged to extract any custom tags
   * used in annotation property to constants that are shared by your codebase.
   */
  String[] tags() default {};

  /** Whether and what type of fix this check provides. */
  ProvidesFix providesFix() default ProvidesFix.NO_FIX;

  /** Types of fixes BugCheckers can provide. */
  public enum ProvidesFix {
    NO_FIX,
    REQUIRES_HUMAN_ATTENTION
    // TODO(epmjohnston): Introduce new values for other kinds of fixes e.g. "no behavioral changes"
  }

  /**
   * The class of bug this bug checker detects.
   *
   * @deprecated This category field hasn't provided much value, as the 'problem domain' of each
   *     BugChecker is evident from the checker itself. We've introduced {@link #tags} as a means to
   *     apply general tags to checks.
   */
  @Deprecated
  Category category() default Category.ONE_OFF;

  public enum Category {
    /** General Java or JDK errors. */
    JDK,
    /** Errors specific to Google Guava. */
    GUAVA,
    /** Errors specific to Google Guice. */
    GUICE,
    /** Errors specific to Dagger. */
    DAGGER,
    /** Errors specific to JUnit. */
    JUNIT,
    /** One-off matchers that are not general errors. */
    ONE_OFF,
    /** JSR-330 errors not specific to Guice. */
    INJECT,
    /** Errors specific to Mockito. */
    MOCKITO,
    /** Errors specific to JMock. */
    JMOCK,
    /** Errors specific to Android. */
    ANDROID,
    /** Errors specific to Protocol Buffers. */
    PROTOBUF,
    /** Errors specific to Truth. */
    TRUTH;
  }

  /**
   * A short summary of the problem that this checker detects. Used for the default compiler error
   * message and for the short description in the generated docs. Should not end with a period, to
   * match javac warning/error style.
   *
   * <p>Markdown syntax is not allowed for this element.
   */
  String summary();

  /**
   * A longer explanation of the problem that this checker detects. Used as the main content in the
   * generated documentation for this checker.
   *
   * <p>Markdown syntax is allowed for this element.
   */
  String explanation() default "";

  SeverityLevel severity();

  public enum SeverityLevel {
    ERROR,
    WARNING,
    SUGGESTION
  }

  /** Whether this checker should be suppressible, and if so, by what means. */
  Suppressibility suppressibility() default Suppressibility.SUPPRESS_WARNINGS;

  public enum Suppressibility {
    /**
     * Can be suppressed using the standard {@code SuppressWarnings("foo")} mechanism. This setting
     * should be used unless there is a good reason otherwise, e.g. security.
     */
    SUPPRESS_WARNINGS(true),
    /** Can be suppressed with a custom annotation on a parent AST node. */
    CUSTOM_ANNOTATION(false),
    /** Cannot be suppressed. */
    UNSUPPRESSIBLE(false);

    private final boolean disableable;

    Suppressibility(boolean disableable) {
      this.disableable = disableable;
    }

    public boolean disableable() {
      return disableable;
    }
  }

  /**
   * A set of custom suppression annotation types to use if suppressibility is
   * Suppressibility.CUSTOM_ANNOTATION.
   */
  Class<? extends Annotation>[] customSuppressionAnnotations() default {};

  /**
   * Generate an explanation of how to suppress the check.
   *
   * <p>This should only be disabled if the check has a non-standard suppression mechanism that
   * requires additional explanation. For example, {@link SuppressWarnings} cannot be applied to
   * packages, so checks that operate at the package level need special treatment.
   */
  public boolean documentSuppression() default true;

  /**
   * Generate examples from test cases.
   *
   * <p>By default, any positive or negative test inputs are included in the generated documentation
   * as examples. That behaviour can be disabled if the test inputs aren't good documentation (for
   * example, because they're testing implementation details of the check and aren't representative
   * of real code).
   *
   * <p>If this feature is disabled, make sure to include some representative examples in the
   * explanation.
   */
  public boolean generateExamplesFromTestCases() default true;
}
