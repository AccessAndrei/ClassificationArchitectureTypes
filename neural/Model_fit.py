from Neural import NeuralUvp
from keras.preprocessing.image import ImageDataGenerator
import tensorflow as tf

input_shape = (250,250, 3)
batch_size = 32
train_datagen = ImageDataGenerator(
    rescale=1. / 255,
    width_shift_range=0.2,
    height_shift_range=0.2,
    horizontal_flip=True,
    brightness_range=(0.5, 1.5),
    shear_range=0.2,
    validation_split=0.1,

    )
train_generator = train_datagen.flow_from_directory(
    'directory',
    target_size=input_shape[:2],
    batch_size=batch_size,
    class_mode='categorical',
    subset='training',
    shuffle=True,
    interpolation='bilinear'
)

validation_generator = train_datagen.flow_from_directory(
    'directory',
    target_size=input_shape[:2],
    batch_size=batch_size,
    class_mode='categorical',
    subset='validation',
    shuffle=True,
    interpolation='bilinear'
)

model = NeuralUvp(train_generator, validation_generator)




