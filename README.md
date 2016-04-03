# Wally


1. Map View
2. + FAB
3. Add Image
4. Set Metadata
5. Image Preview


# Coding styles & standards 

Please refer to official Android [coding standards](https://source.android.com/source/code-style.html).

See how variable declaration differes 
``` Java
public class MyClass {
    public static final int SOME_CONSTANT = 42;
    public int publicField;
    private static MyClass sSingleton;
    int mPackagePrivate;
    private int mPrivate;
    protected int mProtected;
}
```

Also see [Annotation guide](http://developer.android.com/tools/debugging/annotations.html). and [Full Annotation list] (http://developer.android.com/reference/android/support/annotation/package-summary.html)

Annotations like `@NonNull` , `@UiThread` , `@CallSuper` Will be very handfull. 

If you are eager to read further, see [full coding styles & standards] (https://github.com/ribot/android-guidelines/blob/master/project_and_code_guidelines.md). 
# Honor rules. 

Before pushing your code: 

1. Run lint test task. (From gradle panel in the editor, right top panel, Wally -> Tasks -> verification -> lint 
2. Open your generated lint report page and see what new warnings were added. 
3. If there was something new refactor or suppress lint warning. 
4. If you cannot suppress warning leave it with a reason described in your commit message. 

Be carefull when you suppress warning. 
**Never** suppress warning that might crash or break application. 
You can suppress when you definitely have a reason. 
*For example when you know that variable will never be null and lint warns you NullPointerException, you can suppres warning.* 
