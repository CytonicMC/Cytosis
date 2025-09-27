package net.cytonic.cytosis.utils;

public interface SplashProvider {

    SplashProvider DEFAULT = new SplashProvider() {

        @Override
        public String serverError() {
            return "<b><red>SERVER ERROR!</red></b>";
        }

        @Override
        public String success() {
            return "<b><green>SUCCESS!</green></b>";
        }

        @Override
        public String whoops() {
            return "<b><red>WHOOPS!</red></b>";
        }

        @Override
        public String network() {
            return "<b><yellow>NETWORK!</yellow></b>";
        }

        @Override
        public String tip() {
            return "<b><green>TIP!</green></b>";
        }

        @Override
        public String snoop() {
            return "<b><#F873F9>SNOOP!</#F873F9></b>";
        }
    };

    String serverError();

    String success();

    String whoops();

    String network();

    String tip();

    String snoop();
}
