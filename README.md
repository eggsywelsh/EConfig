### EConfig
-----------------------------------------
Based on java Annotation Process Tool technology, According to the annotation information, it is convenient to convert the attribute in the properties file into the corresponding java field value

### Import
-----------------------------------------
in you module's build.gradle then add below dependencies
````
dependencies {
    annotationProcessor 'com.eggsy:econfig-processor:0.0.3'
    compile 'com.eggsy:econfig:0.0.3'
}
````

### Support
EConfig support bind properties String property to 8 java basic type,String,java.util.Date class.And,date formate also supported.

### How to use
In the class file with the ConfigProperty annotation, annotation with java field decorator with public or protected keyword
````
public final class RuntimeConfig {

    private static final String TAG = RuntimeConfig.class.getName();

    @ConfigProperty(name = "min_free_sdcard_size", defaultValue = "20")
    public int minFreeSdcardSize;

    @ConfigProperty(name = "version")
    public int version;

    @ConfigProperty(name = "log_level")
    public int logLevel;

    @ConfigProperty(name = "date_time",format = "yyyy-MM-dd HH:mm")
    public Date dateTime;

    @ConfigProperty(name = "base_sdcard_dir")
    public String baseSdcardDir;

    @ConfigProperty(name = "db_name")
    public String dbName;

    protected void init(Properties prop) {
        EggsyConfig.bindConfig(this, prop);
    }

    private RuntimeConfig() {
    }
}
````

bind the properties into java fields
````
private static RuntimeConfig mConfig;

    public static RuntimeConfig obtain() {
        if (mConfig == null) {
            synchronized (RuntimeConfig.class) {
                if (mConfig == null) {
                    mConfig = new RuntimeConfig();

                    Properties prop = new Properties();
                    try {
                        // get properties file from anywhere
                        prop.load(Env.sApplication.getResources().getAssets().open("runtime_config.properties"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (prop.size() > 0) {
                        // this is the key,bind the properties into RuntimeConfig.java fields
                        mConfig.init(prop);
                    }
                }
            }
        }
        return mConfig;
    }
````

### Sample
Fork or download my github project [EConfig](https://github.com/eggsywelsh/EConfig) to see more Samples