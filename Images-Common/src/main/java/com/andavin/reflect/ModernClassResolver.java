//package com.andavin.reflect;
//
///**
// * @since October 31, 2018
// * @author Andavin
// */
//public class ModernClassResolver implements ClassResolver {
//
//    @Override
//    public String resolve(int depth) {
//        return StackWalker.getInstance().walk(stream -> stream.skip(depth)
//                .findFirst().orElse(null)).getClassName();
//    }
//}
