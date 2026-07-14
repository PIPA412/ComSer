package com.zsc.common.utils.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.zsc.common.utils.StringUtils;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

/**
 * UserAgent解析工具类
 * 
 * @author zsc
 */
public class UserAgentUtils
{
    public static final String UNKNOWN = "";

    // 浏览器正则表达式模式
    private static final Pattern CHROME_PATTERN = Pattern.compile("Chrome/(\\d+)(?:\\.\\d+)*");
    private static final Pattern FIREFOX_PATTERN = Pattern.compile("Firefox/(\\d+)(?:\\.\\d+)*");
    private static final Pattern EDGE_PATTERN = Pattern.compile("Edg(?:e)?/(\\d+)(?:\\.\\d+)*");
    private static final Pattern SAFARI_PATTERN = Pattern.compile("Version/(\\d+)(?:\\.\\d+)*");
    private static final Pattern OPERA_PATTERN = Pattern.compile("Opera/(\\d+)(?:\\.\\d+)*");
    private static final Pattern IE_PATTERN = Pattern.compile("(?:MSIE |Trident/.*rv:)(\\d+)(?:\\.\\d+)*");
    private static final Pattern SAMSUNG_PATTERN = Pattern.compile("SamsungBrowser/(\\d+)(?:\\.\\d+)*");
    private static final Pattern UC_PATTERN = Pattern.compile("UCBrowser/(\\d+)(?:\\.\\d+)*");
    private static final Pattern QQ_PATTERN = Pattern.compile("QQBrowser/(\\d+)(?:\\.\\d+)*");
    private static final Pattern WECHAT_PATTERN = Pattern.compile("MicroMessenger/(\\d+)(?:\\.\\d+)*");
    private static final Pattern BAIDU_PATTERN = Pattern.compile("baidubrowser/(\\d+)(?:\\.\\d+)*");

    // 操作系统正则表达式模式
    private static final Pattern WINDOWS_PATTERN = Pattern.compile("Windows NT (\\d+\\.\\d+)");
    private static final Pattern MACOS_PATTERN = Pattern.compile("Mac OS X (\\d+[_\\d]*)");
    private static final Pattern ANDROID_PATTERN = Pattern.compile("Android (\\d+)(?:\\.\\d+)*");
    private static final Pattern IOS_PATTERN = Pattern.compile("OS[\\s_](\\d+)(?:_\\d+)*");
    private static final Pattern LINUX_PATTERN = Pattern.compile("Linux");
    private static final Pattern CHROMEOS_PATTERN = Pattern.compile("CrOS");

    private static final UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer
            .newBuilder().hideMatcherLoadStats()
            .withCache(5000)
            .showMinimalVersion()
            .withField(UserAgent.AGENT_NAME_VERSION)
            .withField(UserAgent.OPERATING_SYSTEM_NAME_VERSION)
            .build();

    /**
     * 获取客户端浏览器
     */
    public static String getBrowser(String userAgent)
    {
        UserAgent.ImmutableUserAgent iua = userAgentAnalyzer.parse(userAgent);
        String agentNameVersion = iua.get(UserAgent.AGENT_NAME_VERSION).getValue();
        if (StringUtils.isBlank(agentNameVersion) || agentNameVersion.contains("??"))
        {
            return formatBrowser(userAgent);
        }
        return agentNameVersion;
    }

    /**
     * 获取客户端操作系统
     */
    public static String getOperatingSystem(String userAgent)
    {
        UserAgent.ImmutableUserAgent iua = userAgentAnalyzer.parse(userAgent);
        String operatingSystemNameVersion = iua.get(UserAgent.OPERATING_SYSTEM_NAME_VERSION).getValue();
        if (StringUtils.isBlank(operatingSystemNameVersion) || operatingSystemNameVersion.contains("??"))
        {
            return formatOperatingSystem(userAgent);
        }
        return operatingSystemNameVersion;
    }

    /**
     * 全面浏览器检测
     */
    private static String formatBrowser(String browser)
    {
        // Chrome系列浏览器
        Matcher chromeMatcher = CHROME_PATTERN.matcher(browser);
        if (chromeMatcher.find() && (browser.contains("Chrome") || browser.contains("CriOS")))
        {
            return "Chrome" + chromeMatcher.group(1);
        }
        // Firefox
        Matcher firefoxMatcher = FIREFOX_PATTERN.matcher(browser);
        if (firefoxMatcher.find())
        {
            return "Firefox" + firefoxMatcher.group(1);
        }
        // Edge浏览器
        Matcher edgeMatcher = EDGE_PATTERN.matcher(browser);
        if (edgeMatcher.find())
        {
            return "Edge" + edgeMatcher.group(1);
        }
        // Safari浏览器（需排除Chrome）
        Matcher safariMatcher = SAFARI_PATTERN.matcher(browser);
        if (safariMatcher.find() && !browser.contains("Chrome"))
        {
            return "Safari" + safariMatcher.group(1);
        }
        // 微信内置浏览器
        Matcher wechatMatcher = WECHAT_PATTERN.matcher(browser);
        if (wechatMatcher.find())
        {
            return "WeChat" + wechatMatcher.group(1);
        }
        // UC浏览器
        Matcher ucMatcher = UC_PATTERN.matcher(browser);
        if (ucMatcher.find())
        {
            return "UC Browser" + ucMatcher.group(1);
        }
        // QQ浏览器
        Matcher qqMatcher = QQ_PATTERN.matcher(browser);
        if (qqMatcher.find())
        {
            return "QQ Browser" + qqMatcher.group(1);
        }
        // 百度浏览器
        Matcher baiduMatcher = BAIDU_PATTERN.matcher(browser);
        if (baiduMatcher.find())
        {
            return "Baidu Browser" + baiduMatcher.group(1);
        }
        // Samsung浏览器
        Matcher samsungMatcher = SAMSUNG_PATTERN.matcher(browser);
        if (samsungMatcher.find())
        {
            return "Samsung Browser" + samsungMatcher.group(1);
        }
        // Opera浏览器
        Matcher operaMatcher = OPERA_PATTERN.matcher(browser);
        if (operaMatcher.find())
        {
            return "Opera" + operaMatcher.group(1);
        }
        // IE浏览器
        Matcher ieMatcher = IE_PATTERN.matcher(browser);
        if (ieMatcher.find())
        {
            return "Internet Explorer" + ieMatcher.group(1);
        }
        return UNKNOWN;
    }

    /**
     * 检测操作系统
     */
    private static String formatOperatingSystem(String operatingSystem)
    {
        // Windows系统
        Matcher windowsMatcher = WINDOWS_PATTERN.matcher(operatingSystem);
        if (windowsMatcher.find())
        {
            return "Windows" + getWindowsVersionDisplay(windowsMatcher.group(1), operatingSystem);
        }
        // macOS系统
        Matcher macMatcher = MACOS_PATTERN.matcher(operatingSystem);
        if (macMatcher.find())
        {
            String version = macMatcher.group(1).replace("_", ".");
            return "macOS" + extractMajorVersion(version);
        }
        // Android系统
        Matcher androidMatcher = ANDROID_PATTERN.matcher(operatingSystem);
        if (androidMatcher.find())
        {
            return "Android" + extractMajorVersion(androidMatcher.group(1));
        }
        // iOS系统
        Matcher iosMatcher = IOS_PATTERN.matcher(operatingSystem);
        if (iosMatcher.find() && (operatingSystem.contains("iPhone") || operatingSystem.contains("iPad")))
        {
            return "iOS" + extractMajorVersion(iosMatcher.group(1));
        }
        // Linux系统
        if (LINUX_PATTERN.matcher(operatingSystem).find() && !operatingSystem.contains("Android"))
        {
            return "Linux";
        }
        // Chrome OS
        if (CHROMEOS_PATTERN.matcher(operatingSystem).find())
        {
            return "Chrome OS";
        }
        return UNKNOWN;
    }

    /**
     * 提取优化的主版本号
     */
    private static String extractMajorVersion(String fullVersion)
    {
        if (StringUtils.isEmpty(fullVersion))
        {
            return StringUtils.EMPTY;
        }
        try
        {
            // 清理版本号中的非数字字符
            String cleanVersion = fullVersion.replaceAll("[^0-9.]", "");
            String[] parts = cleanVersion.split("\\.");
            if (parts.length > 0)
            {
                String firstPart = parts[0];
                if (firstPart.matches("\\d+"))
                {
                    int version = Integer.parseInt(firstPart);

                    // 处理三位数版本号（如142 -> 14）
                    if (version >= 100)
                    {
                        return String.valueOf(version / 10);
                    }
                    return firstPart;
                }
            }
        }
        catch (NumberFormatException e)
        {
            // 版本号解析失败，返回原始值
        }
        return fullVersion;
    }

    /**
     * Windows版本号显示优化
     *
     * @param version     NT 版本号（如 10.0）
     * @param fullUserAgent 完整的 User-Agent 字符串
     */
    private static String getWindowsVersionDisplay(String version, String fullUserAgent)
    {
        switch (version)
        {
            case "10.0":
                // Win10 和 Win11 的 NT 版本号都是 10.0，需通过其他特征区分
                return detectWin10Or11(fullUserAgent);
            case "6.3":
                return "8.1";
            case "6.2":
                return "8";
            case "6.1":
                return "7";
            case "6.0":
                return "Vista";
            case "5.1":
                return "XP";
            case "5.0":
                return "2000";
            default:
                return extractMajorVersion(version);
        }
    }

    /**
     * 在无法通过 Yauaa 识别时，通过 UA 字符串特征判断是 Win10 还是 Win11
     *
     * <p>两者 NT 版本号都是 10.0，但可通过以下特征辅助判断：</p>
     * <ul>
     *   <li>Chromium 130+ 在 Windows 11 上占有率更高</li>
     *   <li>Edge 浏览器版本的特定范围</li>
     *   <li>部分浏览器会在 UA 的 platform 段携带额外信息</li>
     * </ul>
     */
    private static String detectWin10Or11(String userAgent)
    {
        if (StringUtils.isBlank(userAgent)) {
            return "10";
        }

        // 规则1：通过 Edg 大版本判断（Edge 100+ 普遍预装在 Win11 上）
        Matcher edgeMatcher = Pattern.compile("Edg(?:e)?/(\\d+)").matcher(userAgent);
        if (edgeMatcher.find()) {
            int edgeVersion = Integer.parseInt(edgeMatcher.group(1));
            if (edgeVersion >= 100) {
                return "11";
            }
        }

        // 规则2：提取 Chromium 内核版本，结合其他特征判断
        Matcher chromeMatcher = Pattern.compile("Chrome/(\\d+)").matcher(userAgent);
        int chromeVersion = chromeMatcher.find() ? Integer.parseInt(chromeMatcher.group(1)) : 0;

        // Chrome 130+ 大概率 Win11
        if (chromeVersion >= 130) {
            return "11";
        }

        // 规则3：Win64 + Chrome 100+ 大概率 Win11（夸克等小众浏览器也适用）
        if (chromeVersion >= 100 && userAgent.contains("Win64; x64")) {
            return "11";
        }

        // 无法确定，保持旧行为返回 "10"（避免过度纠正）
        return "10";
    }
}
