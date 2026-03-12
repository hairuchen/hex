package me.chr.hex.core.algorithm;


import java.util.Arrays;

/**
 * @Author: CHR
 * @Date: create in 2026/3/13
 **/
public class Normalization {

    /**
     * L1 归一化（曼哈顿范数归一化）
     * 公式: v_normalized = v / ||v||_1
     * 其中 ||v||_1 = sum(|x_i|)，x_i为向量各维度值（取绝对值求和）
     *
     * @param vector 输入向量数组
     * @return 归一化后的新数组
     * @throws IllegalArgumentException 如果输入为 null 或空
     * @note 结果向量各维度绝对值之和=1；
     *       几乎不用在语义向量（embedding）场景，多用于稀疏向量（如TF-IDF、词频统计）
     */
    public static double[] L1Normalize(double[] vector) {
        // 方法体待实现
        return null;
    }

    /**
     *  L2 归一化（向量模长归一化）
     * 公式: v_normalized = v / ||v||_2
     * 其中 ||v||_2 = sqrt(sum(x_i^2))，x_i为向量各维度值
     *
     * @param vector 输入向量数组
     * @return 归一化后的新数组。如果输入是零向量，返回原向量的副本（全0）。
     * @throws IllegalArgumentException 如果输入为 null 或空
     * @note 结果向量模长=1，余弦相似度等价于点积，值域0~1；
     *       是语义向量（embedding）检索的首选归一化方式，99%向量检索场景使用
     */
    public static double[] L2Normalize(double[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("向量不能为空");
        }

        // 1. 计算 L2 范数 (模长): sqrt(sum(x^2))
        double norm = 0.0;
        for (double value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);

        // 2. 边缘情况处理：如果是零向量，无法归一化，直接返回全0副本
        // 避免除以 0 产生 NaN 或 Infinity
        if (norm == 0.0) {
            return Arrays.copyOf(vector, vector.length);
        }

        // 3. 执行归一化: 每个元素除以模长
        double[] normalizedVector = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalizedVector[i] = vector[i] / norm;
        }

        return normalizedVector;
    }

    /**
     * 最大最小归一化（Min-Max 归一化）
     * 公式: v'_i = (x_i - min(v)) / (max(v) - min(v))
     * 其中 min(v)为向量最小值，max(v)为向量最大值，x_i为向量各维度值
     *
     * @param vector 输入向量数组
     * @return 归一化后的新数组
     * @throws IllegalArgumentException 如果输入为 null 或空
     * @note 把向量每个维度拉到0~1区间；
     *       会破坏向量方向，完全不适合语义向量（embedding）检索；
     *       仅适用于传统特征工程（如数值特征标准化），不适合向量检索场景
     */
    public static double[] minMaxNormalize(double[] vector) {
        // 方法体待实现
        return null;
    }

    /**
     * 标准化（Z-score 标准化）
     * 公式: v'_i = (x_i - μ) / σ
     * 其中 μ为向量均值（sum(x_i)/n），σ为向量标准差（sqrt(sum((x_i-μ)^2)/n)），x_i为向量各维度值
     *
     * @param vector 输入向量数组
     * @return 标准化后的新数组
     * @throws IllegalArgumentException 如果输入为 null 或空
     * @note 结果向量均值=0，方差=1；
     *       会改变向量原始方向，不适合余弦相似度检索；
     *       多用于机器学习模型训练前的特征预处理，不适合语义向量检索
     */
    public static double[] standardize(double[] vector) {
        // 方法体待实现
        return null;
    }
}
